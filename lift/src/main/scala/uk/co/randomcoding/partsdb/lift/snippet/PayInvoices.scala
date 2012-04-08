/*
 * ******************************************************************************
 *  * Copyright (c) ${DATE} RandomCoder <randomcoder@randomcoding.co.uk>
 *  * All rights reserved. This program and the accompanying materials
 *  * are made available under the terms of the Eclipse Public License v1.0
 *  * which accompanies this distribution, and is available at
 *  * http://www.eclipse.org/legal/epl-v10.html
 *  *
 *  * Contributors:
 *  *    RandomCoder - initial API and implementation and/or initial documentation
 *  ******************************************************************************
 */

package uk.co.randomcoding.partsdb.lift.snippet

import net.liftweb.http.StatefulSnippet
import uk.co.randomcoding.partsdb.lift.util.snippet.ErrorDisplay
import net.liftweb.common.Logger
import net.liftweb.util.Helpers._
import com.foursquare.rogue.Rogue._
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.DateHelpers._
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.transaction.{ Transaction, Payment }
import scala.xml.Text
import uk.co.randomcoding.partsdb.core.document.{ DocumentType, Document }

class PayInvoices extends StatefulSnippet with ErrorDisplay with Logger {

  private[this] def availablePayments = (Payment where (_.id exists true) fetch ()) filterNot (_.isFullyAllocated) map (payment => (Some(payment), "%s %s (£%.2f)".format(payment.paymentReference.get, dateString(payment.paymentDate.get), payment.paymentAmount.get)))

  private[this] def paymentSelection: Seq[(Option[Payment], String)] = (None, "Select Payment") :: availablePayments

  private[this] var selectedPayment: Option[Payment] = None

  //private[this] def outstandingTransactions = (Transaction where (_.completionDate exists false) fetch) filter (_.transactionState == "Invoiced")
  private[this] def outstandingTransactions = (Transaction where (_.id exists true) fetch) filter (_.transactionState == "Invoiced")

  private[this] def customersWithUnpaidInvoices = outstandingTransactions map (_.customer.get) map (Customer.findById _) filter (_.isDefined) map (_.get)

  private[this] def customerSelection: Seq[(Option[Customer], String)] = (None, "Select Customer") :: (customersWithUnpaidInvoices map (c => (Some(c), c.customerName.get)))

  private[this] var selectedCustomer: Option[Customer] = None

  override def dispatch = {
    case "render" => render
  }

  def render = {
    debug("Outstanding Transactions: %s".format(outstandingTransactions.mkString("[", "\n", "]")))
    debug("Customers: %s".format(customersWithUnpaidInvoices.mkString("[", "\n", "]")))
    // TODO: Update invoices display
    "#paymentSelect" #> styledAjaxObjectSelect(paymentSelection, None, updateAjaxValue((p: Option[Payment]) => selectedPayment = p)) &
      "#customerSelect" #> styledAjaxObjectSelect(customerSelection, None, updateAjaxValue((c: Option[Customer]) => selectedCustomer = c)) &
      "#unpaidInvoices *" #> renderUnpaidInvoices
  }

  private[this] def unpaidInvoices = selectedCustomer match {
    case Some(c) => {
      val documentIdsFromOutstandingTransactionsForCustomer = outstandingTransactions.filter(_.customer.get == c.id.get).flatMap(_.documents.get)
      val invoicesForCustomer = Document where (_.id in documentIdsFromOutstandingTransactionsForCustomer) and (_.documentType eqs DocumentType.Invoice) fetch
      val paidInvoices = (Payment where (_.id exists true) fetch) flatMap (_.paidInvoices.get.map(_.paidInvoice.get))
      invoicesForCustomer filterNot (paidInvoices contains _.id.get)
    }
    case _ => Nil
  }

  private[this] def renderUnpaidInvoices = unpaidInvoices map { invoice =>
    "#invNum" #> Text(invoice.documentNumber) &
      "#invDate" #> Text(dateString(invoice.createdOn.get)) &
      "#invValue" #> Text("£%.2f".format(invoice.documentValue)) &
      "#remainingBalance" #> Text("£%.2f".format(remainingBalance(invoice)))
  }

  private[this] def remainingBalance(invoice: Document): Double = {
    val paymentsForInvoice = (Payment where (_.id exists true) fetch) flatMap (_.paidInvoices.get) filter (_.paidInvoice.get == invoice.id.get)
    val totalPaidForInvoice: Double = paymentsForInvoice map (_.paymentAmount.get) sum
    val remaining = invoice.documentValue - totalPaidForInvoice

    remaining
  }
}
