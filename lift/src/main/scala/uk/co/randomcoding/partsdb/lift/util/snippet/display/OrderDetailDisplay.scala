/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet.display

import uk.co.randomcoding.partsdb.core.document.Document
import net.liftweb.util.CssSel
import net.liftweb.util.Helpers._
import org.joda.time.DateTime
import uk.co.randomcoding.partsdb.lift.util.LineItemDisplay

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
      renderDocumentTotals(order)
    // TODO: Add next stage button
  })
}