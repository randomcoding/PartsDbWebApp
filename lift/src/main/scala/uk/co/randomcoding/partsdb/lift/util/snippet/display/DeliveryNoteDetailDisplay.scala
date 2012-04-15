/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet.display

import scala.xml.Text

import uk.co.randomcoding.partsdb.core.document.Document
import uk.co.randomcoding.partsdb.lift.util.DateHelpers._
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util._

import net.liftweb.common.Full
import net.liftweb.http.SHtml._
import net.liftweb.util.Helpers._
import net.liftweb.util.CssSel

/**
 * Displays a series of orders using the template `_delivery_note_detail_display.html`
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object DeliveryNoteDetailDisplay extends DocumentTotalsDisplay {
  def apply(deliveryNotes: Seq[Document], transactionId: String): Seq[CssSel] = deliveryNotes map (deliveryNote => {
    val addressLabel = "Delivery Address"
    val (addressText, addressCountry) = deliveryNote.documentAddress.valueBox match {
      case Full(addr) => (addr.addressText.get, addr.country.get)
      case _ => ("No Delivery Address", "No Delivery Address")
    }

    "#deliveryNoteId" #> deliveryNote.documentNumber &
      "#generatedOn" #> dateString(deliveryNote.createdOn.get) &
      "#addressLabel" #> Text(addressLabel) &
      "#billingAddressEntry" #> styledTextArea(addressText, (s: String) => (), readonly) &
      "#billingAddressCountry" #> styledText(addressCountry, (s: String) => (), readonly) &
      "#lineItems" #> LineItemDisplay(deliveryNote.lineItems.get) &
      renderDocumentTotals(deliveryNote) &
      "#raiseInvoice" #> link("/app/invoice?transactionId=%s&deliveryId=%s".format(transactionId, deliveryNote.id.get.toString), () => (), Text("Raise Invoice"))
  })
}