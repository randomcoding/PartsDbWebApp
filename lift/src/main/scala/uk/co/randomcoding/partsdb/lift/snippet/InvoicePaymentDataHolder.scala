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
package uk.co.randomcoding.partsdb.lift.snippet

import net.liftweb.util.ValueCell
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.document.{DocumentType, Document}
import uk.co.randomcoding.partsdb.core.transaction.{Transaction, InvoicePayment, Payment}
import com.foursquare.rogue.Rogue._

/**
 * TODO: Class Documentation
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class InvoicePaymentDataHolder {
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

  private[this] val currentAllocatedAmount = ValueCell[Double](0)

  private[this] val currentCustomer = ValueCell[Option[Customer]](None)

  val unpaidInvoices = currentCustomer.lift(_ match {
    case Some(c) => {
      val documentIdsFromOutstandingTransactionsForCustomer = outstandingTransactions.filter(_.customer.get == c.id.get).flatMap(_.documents.get)
      val invoicesForCustomer = Document where (_.id in documentIdsFromOutstandingTransactionsForCustomer) and (_.documentType eqs DocumentType.Invoice) fetch
      val paidInvoices = (Payment where (_.id exists true) fetch) flatMap (_.paidInvoices.get.map(_.paidInvoice.get))
      invoicesForCustomer filterNot (paidInvoices contains _.id.get)
    }
    case _ => Nil
  })

  private lazy val outstandingTransactions = (Transaction where (_.id exists true) fetch) filter (_.transactionState == "Invoiced")

  private lazy val customersWithUnpaidInvoices = outstandingTransactions map (_.customer.get) map (Customer.findById _) filter (_.isDefined) map (_.get)

  def customerSelection: Seq[(Option[Customer], String)] = (None, "Select Customer") :: (customersWithUnpaidInvoices map (c => (Some(c), c.customerName.get)))


  private[this] val unpaidInvoicesSelectCell = unpaidInvoices.lift(invoices => ("", "Select Invoice") :: (invoices map (inv => (inv.id.get.toString, inv.documentNumber))))

  def unpaidInvoicesSelect = unpaidInvoicesSelectCell.get

  // Accessors
  /**
   * Get the unallocated payment value as a currency formatted string
   */
  val unallocatedPaymentText = unallocatedPayment.lift("£%.2f".format(_))

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

  def currentInvoice = currentInvoiceCell.get

  def currentInvoice_=(inv: Option[Document]) = currentInvoiceCell.set(inv)

  def allocatedToInvoiceText = "%.2f".format(currentAllocatedAmount.get)

  def allocatedToInvoice = currentAllocatedAmount.get

  def allocatedToInvoice_=(value: Double) = currentAllocatedAmount.set(value)

  def selectedCustomer = currentCustomer.get

  def selectedCustomer_=(customer: Option[Customer]) = currentCustomer.set(customer)
}
