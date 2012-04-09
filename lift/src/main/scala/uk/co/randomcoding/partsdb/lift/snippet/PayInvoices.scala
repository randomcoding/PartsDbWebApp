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
import uk.co.randomcoding.partsdb.lift.util.DateHelpers._
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet._

import net.liftweb.http.SHtml._
import net.liftweb.util.Helpers._
import net.liftweb.common.Logger
import net.liftweb.http.js.JsCmds.Noop
import org.bson.types.ObjectId
import net.liftweb.http.{WiringUI, StatefulSnippet}
import scala.xml.{NodeSeq, Text}
import net.liftweb.http.js.JsCmd
import uk.co.randomcoding.partsdb.core.transaction.{InvoicePayment, Payment}

class PayInvoices extends StatefulSnippet with ErrorDisplay with Logger {

  private[this] val dataHolder = new InvoicePaymentDataHolder

  private[this] def availablePayments = (Payment where (_.id exists true) fetch()) filterNot (_.isFullyAllocated) map (payment => (Some(payment), "%s %s (£%.2f)".format(payment.paymentReference.get, dateString(payment.paymentDate.get), payment.paymentAmount.get)))

  private[this] def paymentSelection: Seq[(Option[Payment], String)] = (None, "Select Payment") :: availablePayments

  override def dispatch = {
    case "render" => render
  }

  def render = {
    "#paymentSelect" #> styledAjaxObjectSelect(paymentSelection, None, updateAjaxValue((p: Option[Payment]) => dataHolder.payment = p)) &
      "#customerAndInvoices" #> customerAndInvoices &
      "#paymentRemaining" #> WiringUI.asText(dataHolder.unallocatedPaymentText) &
      "#invoiceToPaySelect" #> WiringUI.toNode(dataHolder.unpaidInvoices)(unpaidInvoices) &
      "#allocateValue" #> WiringUI.toNode(dataHolder.currentAllocatedAmount)(allocatedAmountEntry) &
      "#allocateAllButton" #> styledAjaxButton("Allocate All", setAllocateAllAmountInDataHolder) &
      "#allocateToInvoiceButton" #> styledAjaxButton("Allocate", allocateToInvoice) &
      "#allocatedToInvoices" #> WiringUI.toNode(dataHolder.invoicePayments)(renderAllocatedValues)
  }

  private[this] val renderAllocatedValues: (Seq[InvoicePayment], NodeSeq) => NodeSeq = (payments, nodes) => {
    // This is nasty hack, but will work for now
    val rows = payments map (payment => {
      val paidInvoice = Document.findById(payment.paidInvoice.get)

      val docNumber = paidInvoice match {
        case Some(doc) => doc.documentNumber
        case _ => "Error: No Document"
      }
      val paymentValue = "£%.2f".format(payment.paymentAmount.get)
      val paidInFull = if (payment.paidInFull.get) "Yes" else "No"

      <td>
        {Text(docNumber)}
      </td>
        <td>
          {Text(paymentValue)}
        </td>
        <td>
          {Text(paidInFull)}
        </td>
    })

    val rowsNodes = rows flatMap (row => <tr>
      {row}
    </tr>)

    <table>
      <tr>
        <th>Invoice Number</th>
        <th>Allocated Amount</th>
        <th>Fully Paid?</th>
      </tr>{rowsNodes}
    </table>
  }

  private[this] val allocateToInvoice: () => JsCmd = () => {
    dataHolder.createInvoicePayment()
    dataHolder.resetCurrentValues()
    Noop
  }

  private[this] val setAllocateAllAmountInDataHolder: () => JsCmd = () => {
    dataHolder.currentInvoice match {
      case Some(inv) => dataHolder.allocatedToInvoice = math.min(dataHolder.unallocatedAmount, inv.documentValue)
      case _ => // do nothing
    }
    Noop
  }

  private[this] def allocatedAmountEntry: (Double, NodeSeq) => NodeSeq = (amount, nodes) => {
    styledAjaxText("%.2f".format(amount), updateAjaxValue((amt: String) => dataHolder.allocatedToInvoice = amt))
  }

  private[this] def unpaidInvoices: (List[Document], NodeSeq) => NodeSeq = (docs, nodes) => {
    val selections = ("", "Select Invoice") :: (docs map (doc => (doc.id.get.toString, doc.documentNumber)))
    styledAjaxSelect(selections, "", updateAjaxValue((inv: String) => dataHolder.currentInvoice = Document.findById(new ObjectId(inv))))
  }

  private[this] def customerAndInvoices = idMemoize {
    customerSelect =>
      "#customerSelect" #> styledAjaxObjectSelect(dataHolder.customerSelection, dataHolder.selectedCustomer, updateAjaxValue((c: Option[Customer]) => dataHolder.selectedCustomer = c,
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
