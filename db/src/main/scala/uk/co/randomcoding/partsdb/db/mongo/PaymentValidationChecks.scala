/*
 * Copyright (C) 2012 RandomCoder <randomcoder@randomcoding.co.uk>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *    RandomCoder - initial API and implementation and/or initial documentation
 */
package uk.co.randomcoding.partsdb.db.mongo

import uk.co.randomcoding.partsdb.core.transaction.{InvoicePayment, Payment}
import org.bson.types.ObjectId
import uk.co.randomcoding.partsdb.core.document.Document

/**
 * Contains functions to perform validation checks on Payment operations
 *
 * To perform all checks on a [[uk.co.randomcoding.partsdb.core.transaction.Payment]] and some
 * [[uk.co.randomcoding.partsdb.core.transaction.InvoicePayment]]s, simply use the `apply` method.
 *
 * == Example ==
 * Attempt to pay an invoice for a greater value than the payment
 * {{{
 *   val invoice = Invoice(...)  // An Invoice worth £200
 *   val payment = Payment(100.0, "Pay 1", Nil)
 *   val invoicePayment = InvoicePayment(invoice, 200.0)
 *
 *   val response = PaymentValidationChecks(payment, Seq(invoicePayment))
 *   // response is Seq("Error Message")
 * }}}
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object PaymentValidationChecks {

  /**
   * A convenience method to provide quick access to all the checks defined in this object
   *
   * @param payment The [[uk.co.randomcoding.partsdb.core.transaction.Payment]] that is being checked
   * @param invoicePayments The [[uk.co.randomcoding.partsdb.core.transaction.InvoicePayment]]s that would be paid by the provided `payment`
   *
   * @return The results of applying all the checks to the input `payment` and `invoicePayment`s
   */
  def apply(payment: Payment, invoicePayments: Seq[InvoicePayment]): Seq[String] = paymentErrorChecks flatMap (_(payment, invoicePayments))

  private[this] def paymentErrorChecks: Seq[(Payment, Seq[InvoicePayment]) => Seq[String]] = Seq(
    invoicesToBePaidAreInDatabase,
    invoicesToBePaidAreNotAlreadyClosed,
    invoicePaymentIsForTheOutstandingBalanceOfTheInvoiceOrLess,
    paymentHasSufficientBalanceToPayTheInvoicePayments
  )

  /**
   * Checks that the `payment` has sufficient remaining unallocated value to satisfy all the `invoicePayments`
   *
   * If the payment is fully allocated then this will return a message to say so.
   * If the payment has insufficient remaining to pay al the `invoicePayments` then this will return a message to that effect instead.
   * Otherwise this will return an empty `Seq` indicating no errors
   */
  val paymentHasSufficientBalanceToPayTheInvoicePayments: (Payment, Seq[InvoicePayment]) => Seq[String] = (payment, invoicePayments) => {
    val amountToAllocate = invoicePayments.foldLeft(0d)(_ + _.paymentAmount.get)
    payment.isFullyAllocated match {
      case true => Seq("Payment %s has already been fully allocated. It is not possible to pay any more invoices with it".format(payment.paymentReference.get))
      case false => if (payment.unallocatedBalance >= amountToAllocate) Nil else Seq("The payment does not have sufficient available balance to pay £%.2f".format(amountToAllocate))
    }
  }

  private[this] def findInvoiceInDatabase(invoiceId: ObjectId) = Document findById (invoiceId)

  val invoicesToBePaidAreInDatabase: (Payment, Seq[InvoicePayment]) => Seq[String] = (payment, invoicePayments) => {
    for {
      invPayment <- invoicePayments
      if findInvoiceInDatabase(invPayment.paidInvoice.get) isEmpty
    }
    yield {
      "Invoice for payment of £%.2f was not found in the database.\nObject Id: %s".format(invPayment.paymentAmount.get, invPayment.paidInvoice.get)
    }
  }

  val invoicesToBePaidAreNotAlreadyClosed: (Payment, Seq[InvoicePayment]) => Seq[String] = (payment, invoicePayments) => {
    for {
      invPayment <- invoicePayments
      val inv = findInvoiceInDatabase(invPayment.paidInvoice.get)
      if inv.isDefined
      if inv.get.editable.get == false
    }
    yield {
      "Invoice %s has already been closed".format(inv.get.documentNumber)
    }
  }

  val invoicePaymentIsForTheOutstandingBalanceOfTheInvoiceOrLess: (Payment, Seq[InvoicePayment]) => Seq[String] = (payment, invoicePayments) => {
    for {
      invPayment <- invoicePayments
      val inv = findInvoiceInDatabase(invPayment.paidInvoice.get)
      if inv.isDefined
      val outstandingBalance = inv.get.remainingBalance
      if invPayment.paymentAmount.get > outstandingBalance
    }
    yield {
      "Invoice %s only has £%.2f outstanding. It is not possible to allocate £%.2f to this invoice".format(inv.get.documentNumber, outstandingBalance, invPayment.paymentAmount.get)
    }
  }
}
