package uk.co.randomcoding.partsdb.lift.util.snippet

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

import net.liftweb.util.ValueCell
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.document.{DocumentType, Document}
import uk.co.randomcoding.partsdb.core.transaction.{Transaction, InvoicePayment, Payment}
import com.foursquare.rogue.Rogue._
import net.liftweb.util.Helpers.asDouble
import net.liftweb.common.{Logger, Full}

/**
 * TODO: Class Documentation
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

  // Correct version - cannot use until #96 is fixed
  //private[this] def outstandingTransactions = (Transaction where (_.completionDate exists false) fetch) filter (_.transactionState == "Invoiced")
  private lazy val outstandingTransactions = (Transaction where (_.id exists true) fetch) filter (_.transactionState == "Invoiced")

  private lazy val customersWithUnpaidInvoices = outstandingTransactions map (_.customer.get) map (Customer.findById _) filter (_.isDefined) map (_.get)

  private[this] val unpaidInvoicesSelectCell = unpaidInvoices.lift(invoices => (None, "Select Invoice") :: (invoices map (inv => (Some(inv), inv.documentNumber))))

  // Accessors
  /**
   * Get the unallocated payment value as a currency formatted string
   */
  val unallocatedPaymentText = unallocatedPayment.lift("£%.2f".format(_))

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
  val unpaidInvoices = currentCustomer.lift(_ match {
    case Some(c) => {
      val documentIdsFromOutstandingTransactionsForCustomer = outstandingTransactions.filter(_.customer.get == c.id.get).flatMap(_.documents.get)
      val invoicesForCustomer = Document where (_.id in documentIdsFromOutstandingTransactionsForCustomer) and (_.documentType eqs DocumentType.Invoice) fetch
      val paidInvoices = (Payment where (_.id exists true) fetch) flatMap (_.paidInvoices.get.map(_.paidInvoice.get))
      invoicesForCustomer filterNot (paidInvoices contains _.id.get)
    }
    case _ => Nil
  })
}
