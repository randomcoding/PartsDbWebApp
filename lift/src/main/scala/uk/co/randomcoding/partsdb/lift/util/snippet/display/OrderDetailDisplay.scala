/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet.display

import scala.xml.Text

import org.joda.time.DateTime

import uk.co.randomcoding.partsdb.core.document.Document
import uk.co.randomcoding.partsdb.lift.util._

import net.liftweb.http.SHtml._
import net.liftweb.util.Helpers._
import net.liftweb.util.CssSel

/**
 * Displays a series of orders using the template `_order_detail_display.html`
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object OrderDetailDisplay extends DocumentTotalsDisplay {
  def apply(orders: Seq[Document], transactionId: String): Seq[CssSel] = orders map (order => {
    "#orderId" #> order.documentNumber &
      "#orderedOn" #> new DateTime(order.createdOn.get).toString("dd/MM/yyyy") &
      "#lineItems" #> LineItemDisplay(order.lineItems.get) &
      renderDocumentTotals(order) &
      "#raiseDelivery" #> link("/app/delivery?transactionId=%s".format(transactionId), () => (), Text("Raise Delivery Note"))
  })
}