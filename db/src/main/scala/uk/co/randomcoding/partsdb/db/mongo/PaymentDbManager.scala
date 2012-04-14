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

import uk.co.randomcoding.partsdb.core.transaction.{Transaction, InvoicePayment, Payment}
import com.foursquare.rogue.Rogue._
import uk.co.randomcoding.partsdb.core.document.{DocumentType, Document}


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
    PaymentValidationChecks(payment, invoicePayments) match {
      case Nil => processCommitOfPayment(payment, invoicePayments: _*)
      case errors => errors map (PaymentFailed(_))
    }
  }

  /*
   * By the time we get here all the validation checks have passed so all invoices referenced in the `invoicePayments` are in the database
   * and the payments do not overpay the invoices.
   *
   * Here we add the payments to the database and if that went ok then update the relevant invoices and transactions
   */
  private[this] def processCommitOfPayment(payment: Payment, invoicePayments: InvoicePayment*): Seq[PaymentResult] = {
    Payment.add(payment) match {
      case Some(pmt) => Payment.addInvoices(payment.id.get, invoicePayments) match {
        case Some(p) => recordPaymentsAgainstInvoicesAndUpdateTransactions(invoicePayments)
        case _ => Seq(PaymentFailed("Failed to add invoice payments to database"))
      }
      case _ => Seq(PaymentFailed("Failed to add Payment %s for £%.2f to the database".format(payment.paymentReference.get, payment.paymentAmount.get)))
    }
  }

  /**
   * This processes the payments to see if any of the invoices and transactions can be closed
   *
   * @param invoicePayments The [[uk.co.randomcoding.partsdb.core.transaction.InvoicePayment]]s to be processed
   *
   * @return The [[uk.co.randomcoding.partsdb.db.mongo.PaymentResult]]s from the operations.
   *         If there are no errors processing the invoices and transactions then this will just contain
   *         a [[uk.co.randomcoding.partsdb.db.mongo.PaymentSuccessful]].
   */
  private[this] def recordPaymentsAgainstInvoicesAndUpdateTransactions(invoicePayments: Seq[InvoicePayment]) = {
    val invoiceUpdateErrors = processInvoiceClosureForPayments(invoicePayments)
    val transactionClosureErrors = processTransactionClosureForPayments(invoicePayments)

    (invoiceUpdateErrors ++ transactionClosureErrors) match {
      case Nil => Seq(PaymentSuccessful)
      case errors => errors
    }
  }

  private[this] def processTransactionClosureForPayments(invoicePayments: Seq[InvoicePayment]): Seq[PaymentResult] = {
    val resultsOfTransactionClosure = invoicePayments map (invPayment => (invPayment, closeTransactionContainingInvoiceIfFullyPaid(invPayment)))

    resultsOfTransactionClosure filterNot (_._2 == PaymentSuccessful) map (_._2)
  }

  private[this] def closeTransactionContainingInvoiceIfFullyPaid(invPayment: InvoicePayment): PaymentResult = {
    val invoiceId = invPayment.paidInvoice.get

    Transaction where (_.documents contains invoiceId) get() match {
      case Some(t) => closeTransactionIfFullyPaid(t) match {
        case Some(_) => PaymentSuccessful
        case _ => PaymentFailed("Error when attempting to close transaction %s. PLease file an error report".format(t.shortName.get))
      }
      case None => PaymentFailed("Unable to retrieve Tranasction containing invoice %s from the database".format(Document.findById(invoiceId).get.documentNumber))
    }
  }

  /**
   * Closes a transaction if all the invoices are fully paid, all documents are closed and the value of the orders is
   * matched by the delivery notes and the value of the delivery notes is matched by the invoices
   *
   * @param transaction The [[uk.co.randomcoding.partsdb.core.transaction.Transaction]] to attempt to close
   * @return An `Option[`[[uk.co.randomcoding.partsdb.core.transaction.Transaction]]']'
   *         This will contain the updated transaction if it was closed, or the original [[uk.co.randomcoding.partsdb.core.transaction.Transaction]] if it was not updated.
   *         If there was a problem with the close operation, this will be `None`
   */
  private[this] def closeTransactionIfFullyPaid(transaction: Transaction): Option[Transaction] = {
    val documents = Document where (_.id in transaction.documents.get) fetch()
    val allDocumentsClosed = documents filter (_.editable.get == true) isEmpty
    val allInvoicesPaid = documents filter (_.documentType.get == DocumentType.Invoice) filter (_.remainingBalance > 0) isEmpty

    val orderValue = transaction.valueOfDocuments(DocumentType.Order)
    val dNoteValue = transaction.valueOfDocuments(DocumentType.DeliveryNote)
    val invoiceValue = transaction.valueOfDocuments(DocumentType.Invoice)

    val okToCloseTransaction = (invoiceValue == dNoteValue && dNoteValue == orderValue && allDocumentsClosed && allInvoicesPaid)

    if (okToCloseTransaction) Transaction.close(transaction.id.get) else Some(transaction)
  }


  private[this] def processInvoiceClosureForPayments(invoicePayments: Seq[InvoicePayment]): Seq[PaymentResult] = {
    val resultsOfInvoiceUpdate = invoicePayments map (invPayment => (invPayment, closeInvoiceIfFullyPaid(invPayment)))

    resultsOfInvoiceUpdate filter (_._2 isEmpty) match {
      case Nil => List.empty
      case emptyResponses => emptyResponses map (response => PaymentFailed("Failed to record payment of £%.2f against invoice with id %s".format(response._1.paymentAmount.get, response._1.paidInvoice.get)))
    }
  }

  /**
   * Closes the invoice associated to the provided invoice payment if the invoice has been fully paid
   *
   * By now the [[uk.co.randomcoding.partsdb.core.transaction.InvoicePayment]]s have been added to the database successfully,
   * so the remaining balance calculation can be used to determine if the invoice is paid or not
   *
   * @param invPayment The [[uk.co.randomcoding.partsdb.core.transaction.InvoicePayment]] for the invoice to check if it can be closed
   * @return An option containing the modified invoice, of the original invoice if it was not updated.
   *         If there is an error in the close operation then this will be `None`
   */
  private[this] def closeInvoiceIfFullyPaid(invPayment: InvoicePayment): Option[Document] = {
    // if the payment is for the remaining balance of the invoice close the invoice
    val invoiceId = invPayment.paidInvoice.get
    val invoice = Document.findById(invoiceId).get

    if (invoice.remainingBalance == 0.0) Document.close(invoiceId) else Some(invoice)
  }
}

sealed abstract class PaymentResult

case object PaymentSuccessful extends PaymentResult

case class PaymentFailed(failureReason: String) extends PaymentResult
