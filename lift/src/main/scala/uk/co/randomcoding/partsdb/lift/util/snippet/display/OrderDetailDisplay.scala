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

import net.liftweb.util.Helpers._
import net.liftweb.util.CssSel

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
      "#raiseDelivery" #> (if (order.editable.get) buttonLink("Raise Delivery Note", "/app/delivery?transactionId=%s".format(transactionId)) else Text("")) &
      renderPrintDocument(order)
  })
}