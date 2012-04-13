/*
 * Copyright (c) 2012 RandomCoder <randomcoder@randomcoding.co.uk>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    RandomCoder - initial API and implementation and/or initial documentation
 */
package uk.co.randomcoding.partsdb.db.mongo

import uk.co.randomcoding.partsdb.core.document.Document
import uk.co.randomcoding.partsdb.core.transaction.{Transaction, InvoicePayment, Payment}
import com.foursquare.rogue.Rogue._

/**
 * Manages submitting invoicePayments to the database.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object PaymentDbManager {

  /**
   * Commit a payment to the database, adding some invoice payments to it.
   *
   * @param payment The [[uk.co.randomcoding.partsdb.core.transaction.Payment]] to add
   * @param invoicePayments The [[uk.co.randomcoding.partsdb.core.transaction.InvoicePayment]]s to add to the `payment`
   */
  def commitPayment(payment: Payment, invoicePayments: InvoicePayment*): Seq[PaymentResult] = {
    paymentErrorChecks flatMap (_(payment, invoicePayments)) match {
      case Nil => processCommitOfPayment(payment)
      case errors => errors map (PaymentFailed(_))
    }
  }

  private[this] def processCommitOfPayment(payment: Payment, invoicePayments: InvoicePayment*): Seq[PaymentResult] = {

    // All ok then update records and commit
    Payment.addInvoices(payment.id.get, invoicePayments)

    //TODO: Set invoices to paid and transaction to closed
    val fullyPaid = invoicePayments filter (_.paidInFull getOrElse false) map (_.paidInvoice.get)

    fullyPaid foreach (Document close)

    val fullyPaidInvoices = fullyPaid map (Document findById _) filter (_.isDefined) map (_.get)
    closeTransactionsForFullyPaidInvoices(fullyPaidInvoices)

    //TODO: Change returned list from Nil
    Nil
  }

  private[this] def paymentErrorChecks: Seq[(Payment, Seq[InvoicePayment]) => Seq[String]] = Seq(
    paymentHasSufficientBalanceToPayTheInvoicePayments
  )

  private[this] val paymentHasSufficientBalanceToPayTheInvoicePayments: (Payment, Seq[InvoicePayment]) => Seq[String] = (payment, invoicePayments) => {
    val amountToAllocate = invoicePayments.foldLeft(0d)(_ + _.paymentAmount.get)
    if (payment.unallocatedBalance >= amountToAllocate) Nil else Seq("The payment does not have sufficient available balance to pay £%.2f".format(amountToAllocate))
  }

  private[this] def closeTransactionsForFullyPaidInvoices(paidInvoices: Seq[Document]) {
    val affectedTransactions = Transaction where (_.documents in (paidInvoices.map(_.id.get))) fetch

    affectedTransactions foreach (transaction => {
      // get the value of the orders

      // get the value of the delivery notes

      // get the value of the invoices

      // if they match the transaction is completed
    })
  }
}

sealed abstract class PaymentResult

case object PaymentSuccessful extends PaymentResult

case class PaymentFailed(failureReason: String) extends PaymentResult
