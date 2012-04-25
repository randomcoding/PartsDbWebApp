/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet.display

import scala.xml.Text
import uk.co.randomcoding.partsdb.core.document.Document
import uk.co.randomcoding.partsdb.lift.util._
import uk.co.randomcoding.partsdb.lift.util.DateHelpers._
import net.liftweb.http.SHtml._
import net.liftweb.util.Helpers._
import net.liftweb.util.CssSel
import uk.co.randomcoding.partsdb.lift.util.snippet.PrintDocumentSnippet

/**
 * Displays a series of orders using the template `_order_detail_display.html`
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object OrderDetailDisplay extends DocumentTotalsDisplay with PrintDocumentSnippet {
  def apply(orders: Seq[Document], transactionId: String): Seq[CssSel] = orders map (order => {
    "#orderId" #> order.documentNumber &
      "#orderedOn" #> dateString(order.createdOn.get) &
      "#lineItems" #> LineItemDisplay(order.lineItems.get) &
      renderDocumentTotals(order) &
      "#raiseDelivery" #> link("/app/delivery?transactionId=%s".format(transactionId), () => (), Text("Raise Delivery Note")) &
      renderPrintDocument(order)
  })
}