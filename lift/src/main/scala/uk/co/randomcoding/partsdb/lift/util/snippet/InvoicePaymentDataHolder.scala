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
package uk.co.randomcoding.partsdb.lift.util.snippet

import net.liftweb.util.ValueCell
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.document.{ DocumentType, Document }
import uk.co.randomcoding.partsdb.core.transaction.{ Transaction, InvoicePayment, Payment }
import com.foursquare.rogue.Rogue._
import net.liftweb.util.Helpers.asDouble
import net.liftweb.common.{ Logger, Full }
import java.util.Date

/**
 * Data holder for invoice the payment page.
 *
 * This manages all the required state etc. to maintain the widgets on the page and to provide `WiringUI` updates
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class InvoicePaymentDataHolder extends Logger {
  // Data Holders
  private[this] val paymentCell = ValueCell[Option[Payment]](None)

  private[this] val paymentsCell = ValueCell[Seq[InvoicePayment]](Nil)

  private[this] val unallocatedPayment = paymentCell.lift(paymentsCell)((payment, payments) => {
    payment match {
      case Some(p) => p.paymentAmount.get - (payments.foldLeft(0.0d)(_ + _.paymentAmount.get))
      case _ => 0.0d
    }
  })

  private[this] val currentInvoiceCell = ValueCell[Option[Document]](None)

  private[this] val currentCustomer = ValueCell[Option[Customer]](None)

  private[this] def outstandingTransactions = Transaction.where(_.completionDate eqs Transaction.defaultCompletionDate).fetch().filter(_.transactionState == "Invoiced")

  private[this] def customersWithUnpaidInvoices = Customer where (_.id in outstandingTransactions.map(_.customer.get)) fetch ()

  private[this] def unpaidInvoicesSelectCell = unpaidInvoices.lift(invoices => (None, "Select Invoice") :: (invoices map (inv => (Some(inv), inv.documentNumber))))

  private[this] def unpaidInvoicesForCustomer(c: Customer) = {
    val docIdsForCustomerTransactions = outstandingTransactions.filter(_.customer.get == c.id.get).flatMap(_.documents.get)
    val docs = Document where (_.id in docIdsForCustomerTransactions) and (_.documentType eqs DocumentType.Invoice) and (_.editable eqs true) fetch ()

    docs filter (_.remainingBalance > 0.0)
  }

  // Accessors
  /**
   * Get the unallocated payment value as a currency formatted string
   */
  val unallocatedPaymentText = unallocatedPayment.lift("£%.2f".format(_))

  /**
   * The currently recorded invoice invoicePayments
   */
  val invoicePayments = ValueCell[Seq[InvoicePayment]](Nil)

  /**
   * Generates the values for use in a select combo box for invoice selection
   *
   * @return A `List[(Option[`[[uk.co.randomcoding.partsdb.core.document.Document]]`], String)]` with all unpaid invoices for the current customer
   */
  def unpaidInvoicesSelect = unpaidInvoicesSelectCell.get

  /**
   * @return The `Option[Payment]` of the currently stored [[uk.co.randomcoding.partsdb.core.transaction.Payment]]
   */
  def payment = paymentCell.get

  /**
   * Set a new [[uk.co.randomcoding.partsdb.core.transaction.Payment]] to the current payment
   *
   * @param p The payment to use
   */
  def payment_=(p: Option[Payment]) = paymentCell.set(p)

  /**
   * @return An `Option[Document]` with the currently selected invoice for allocation
   */
  def currentInvoice = currentInvoiceCell.get

  /**
   * Set the invoice to allocate a payment value to
   * @param inv The invoice
   */
  def currentInvoice_=(inv: Option[Document]) = currentInvoiceCell.set(inv)

  /**
   * @return The amount currently to be allocated as a currency string
   */
  def allocatedToInvoiceText = "%.2f".format(currentAllocatedAmount.get)

  /**
   * @return The amount currently to be allocated as a double
   */
  def allocatedToInvoice = currentAllocatedAmount.get

  /**
   * Setter for the amount to be allocated
   * @param value The amount to set to be allocated
   */
  def allocatedToInvoice_=(value: Double) {
    currentAllocatedAmount.set(value)
  }

  /**
   * Setter for the amount to be allocated.
   *
   * If the `value` does not parse as a `Double` then the allocated value is not updated
   *
   * @param value The amount to set to be allocated as a String
   */
  def allocatedToInvoice_=(value: String) {
    asDouble(value) match {
      case Full(d) => allocatedToInvoice = d
      case _ => // do nothing
    }
  }

  /**
   * @return The current [[uk.co.randomcoding.partsdb.core.customer.Customer]]
   */
  def selectedCustomer = currentCustomer.get

  /**
   * Set the current customer
   *
   * @param customer The new value for the current customer
   */
  def selectedCustomer_=(customer: Option[Customer]) {
    currentCustomer.set(customer)
  }

  /**
   * @return The amount that is currently unallocated from the [[uk.co.randomcoding.partsdb.core.transaction.Payment]]
   */
  def unallocatedAmount = unallocatedPayment.get

  /**
   * Generates the values for use in a select combo box for customer selection
   *
   * @return A `List[(Option[`[[uk.co.randomcoding.partsdb.core.customer.Customer]]`], String)]` with all customers that have outstanding invoices
   */
  def customerSelection: Seq[(Option[Customer], String)] = (None, "Select Customer") :: (customersWithUnpaidInvoices map (c => (Some(c), c.customerName.get)))

  /**
   * The data cell for the amount that will be allocated to the generated payment
   */
  val currentAllocatedAmount = ValueCell[Double](0)

  /**
   * The invoices for the current customer that are not yet fully paid
   */
  val unpaidInvoices = currentCustomer.lift(invoicePayments)((c: Option[Customer], p: Seq[InvoicePayment]) => (c, p) match {
    case (Some(customer), Nil) => unpaidInvoicesForCustomer(customer)
    case (Some(customer), payments) => unpaidInvoicesForCustomer(customer) filterNot (inv => payments.exists(_.paidInvoice.get == inv.id.get))
    case (_, _) => Nil
  })

  def createInvoicePayment() {
    currentInvoice match {
      case Some(inv) => {
        val payment = InvoicePayment(currentInvoice.get, allocatedToInvoice)
        invoicePayments.atomicUpdate(payments => payments find (_ == payment) match {
          case Some(p) => payments
          case _ => payments :+ payment
        })
      }
      case _ => // Do Nothing
    }
  }

  /**
   * Reset all the data values to their default
   */
  def resetCurrentValues() {
    currentAllocatedAmount set 0
    currentInvoiceCell set None
  }

  def payments = invoicePayments.get

  /**
   * Validation checks for the current set of values
   *
   * These are to be used in [[uk.co.randomcoding.partsdb.lift.util.snippet.DataValidation]]
   *
   * @return The functions that will validate the input of the current allocation.
   */
  def createAllocationValidationChecks() = Seq(amountAllocated, allocatedAmountIsLessThanInvoiceAmount, allocatedAmountIsAvailableInRemainingPayment)

  private val amountAllocated = () => if (allocatedToInvoice > 0d) Nil else Seq("Please enter an amount to allocate to the invoice")

  private val allocatedAmountIsLessThanInvoiceAmount = () => currentInvoice match {
    case Some(inv) => if (inv.documentValue < allocatedToInvoice) Seq("You cannot allocate more than the invoice value") else Nil
    case _ => Nil
  }

  private val allocatedAmountIsAvailableInRemainingPayment = () => if (allocatedToInvoice > unallocatedAmount) Seq("You cannot allocate more than the remaining amount") else Nil
}
