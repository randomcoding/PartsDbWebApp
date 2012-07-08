/*
 * Copyright (C) 2012 RandomCoder <randomcoder@randomcoding.co.uk>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *    RandomCoder - initial API and implementation and/or initial documentation
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
