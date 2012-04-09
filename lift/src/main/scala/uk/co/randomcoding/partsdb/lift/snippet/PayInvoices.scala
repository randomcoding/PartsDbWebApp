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


import com.foursquare.rogue.Rogue._

import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.document.Document
import uk.co.randomcoding.partsdb.core.transaction.Payment
import uk.co.randomcoding.partsdb.lift.util.DateHelpers._
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet._

import net.liftweb.http.SHtml._
import net.liftweb.util.Helpers._
import net.liftweb.common.{Full, Logger}
import net.liftweb.http.js.JsCmds.Noop
import org.bson.types.ObjectId
import net.liftweb.http.{WiringUI, StatefulSnippet}
import scala.xml.{NodeSeq, Text}

class PayInvoices extends StatefulSnippet with ErrorDisplay with Logger {

  private[this] val dataHolder = new InvoicePaymentDataHolder

  private[this] def availablePayments = (Payment where (_.id exists true) fetch()) filterNot (_.isFullyAllocated) map (payment => (Some(payment), "%s %s (£%.2f)".format(payment.paymentReference.get, dateString(payment.paymentDate.get), payment.paymentAmount.get)))

  private[this] def paymentSelection: Seq[(Option[Payment], String)] = (None, "Select Payment") :: availablePayments

  // Correct version - cannot use until #96 is fixed
  //private[this] def outstandingTransactions = (Transaction where (_.completionDate exists false) fetch) filter (_.transactionState == "Invoiced")

  override def dispatch = {
    case "render" => render
  }

  def render = {
    /*debug("Outstanding Transactions: %s".format(outstandingTransactions.mkString("[", "\n", "]")))
    debug("Customers: %s".format(customersWithUnpaidInvoices.mkString("[", "\n", "]")))*/

    "#paymentSelect" #> styledAjaxObjectSelect(paymentSelection, None, updateAjaxValue((p: Option[Payment]) => dataHolder.payment = p)) &
      "#customerAndInvoices" #> customerAndInvoices &
      "#paymentRemaining" #> WiringUI.asText(dataHolder.unallocatedPaymentText) &
      "#invoiceToPaySelect" #> WiringUI.toNode(dataHolder.unpaidInvoices)(unpaidInvoices) &
      "#allocateValue" #> styledAjaxText(dataHolder.allocatedToInvoiceText, updateAjaxValue((amt: String) => {
        asDouble(amt) match {
          case Full(d) => dataHolder.allocatedToInvoice = d
          case _ => // do nothing
        }
      })) &
      "#allocateToInvoiceButton" #> styledAjaxButton("Allocate", () => Noop) // TODO: provide update function
  }

  /*private[this] def invoicesToPaySelectContent = WiringUI.toNode(dataHolder.unpaidInvoicesSelect, "#invoiceToPaySelect" #>
    styledAjaxSelect(dataHolder.unpaidInvoicesSelect, "", updateAjaxValue((inv: String) => dataHolder.currentInvoice = Document.findById(new ObjectId(inv)))))*/


  private[this] def unpaidInvoices: (List[Document], NodeSeq) => NodeSeq = (docs, nodes) => {
    val selections = ("", "Select Invoice") :: (docs map (doc => (doc.id.get.toString, doc.documentNumber)))
    styledAjaxSelect(selections, "", updateAjaxValue((inv: String) => dataHolder.currentInvoice = Document.findById(new ObjectId(inv))))
  }

  //private[this] def refreshInvoicesToAllocateOptions(): JsCmd = ReplaceOptions("invoiceToPaySelect", dataHolder.unpaidInvoicesSelect.toList, Full(""))

  private[this] def customerAndInvoices = idMemoize {
    customerSelect => "#customerSelect" #> styledAjaxObjectSelect(dataHolder.customerSelection, dataHolder.selectedCustomer, updateAjaxValue((c: Option[Customer]) => dataHolder.selectedCustomer = c,
      ajaxInvoke(customerSelect.setHtml _)._2.cmd)) &
      "#unpaidInvoices" #> renderUnpaidInvoices
  }

  private[this] def renderUnpaidInvoices = dataHolder.unpaidInvoices.get map (invoice =>
    "#invNum" #> Text(invoice.documentNumber) &
      "#invDate" #> Text(dateString(invoice.createdOn.get)) &
      "#invValue" #> Text("£%.2f".format(invoice.documentValue)) &
      "#remainingBalance" #> Text("£%.2f".format(remainingBalance(invoice))))

  private[this] def remainingBalance(invoice: Document): Double = {
    val paymentsForInvoice = (Payment where (_.id exists true) fetch) flatMap (_.paidInvoices.get) filter (_.paidInvoice.get == invoice.id.get)
    val totalPaidForInvoice: Double = paymentsForInvoice map (_.paymentAmount.get) sum

    invoice.documentValue - totalPaidForInvoice
  }
}
