/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet.display

import scala.xml.Text

import uk.co.randomcoding.partsdb.core.document.Document
import uk.co.randomcoding.partsdb.lift.util.DateHelpers._
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet._
import uk.co.randomcoding.partsdb.lift.util._

import net.liftweb.common.Full
import net.liftweb.util.Helpers._
import net.liftweb.util.CssSel

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object InvoiceDetailDisplay extends DocumentTotalsDisplay with PrintDocumentSnippet {
  def apply(invoices: Seq[Document], transactionId: String): Seq[CssSel] = invoices map (invoice => {
    val addressLabel = "Invoice Address"
    val (addressText, addressCountry) = invoice.documentAddress.valueBox match {
      case Full(addr) => (addr.addressText.get, addr.country.get)
      case _ => ("No Invoice Address", "No Invoice Address")
    }

    "#invoiceId" #> invoice.documentNumber &
      "#invoiceDate" #> dateString(invoice.createdOn.get) &
      "#addressLabel" #> Text(addressLabel) &
      "#addressLabel" #> Text(addressLabel) &
      "#billingAddressEntry" #> styledTextArea(addressText, (s: String) => (), readonly) &
      "#billingAddressCountry" #> styledText(addressCountry, (s: String) => (), readonly) &
      "#lineItems" #> LineItemDisplay(invoice.lineItems.get) &
      renderDocumentTotals(invoice) &
      "#payInvoice" #> (if (invoice.editable.get) buttonLink("Pay Invoice", "/app/payInvoice?transactionId=%s&invoiceId=%s".format(transactionId, invoice.id.get.toString)) else Text("")) &
      renderPrintDocument(invoice)
  })
}