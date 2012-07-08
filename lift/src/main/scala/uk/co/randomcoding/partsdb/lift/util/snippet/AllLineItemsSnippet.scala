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

/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet

import uk.co.randomcoding.partsdb.core.document.LineItem
import uk.co.randomcoding.partsdb.lift.model.document.LineItemsDataHolder
import uk.co.randomcoding.partsdb.lift.util._
import net.liftweb.http.js.JsCmds.Replace
import net.liftweb.http.js.JsCmd
import net.liftweb.util.Helpers._
import net.liftweb.util.CssSel
import scala.xml.Attribute
import scala.xml.Text
import scala.xml.Null
import scala.xml.NodeSeq
import net.liftweb.common.Logger

/**
 * A simple snippet to display all line items, either from a [[uk.co.randomcoding.partsdb.lift.model.document.LineItemsDataHolder]] or passed in directly
 * via [[uk.co.randomcoding.partsdb.lift.util.snippet.AllLineItemsSnippet#refreshLineItemDisplay(Seq[LineItem])]] or
 * [[uk.co.randomcoding.partsdb.lift.util.snippet.AllLineItemsSnippet#renderAllLineItems(Seq[LineItem])]]
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait AllLineItemsSnippet extends Logger {
  val dataHolder: LineItemsDataHolder

  /**
   * Updates the content of the element with the id `lineItems` to contain the results of rendering all the line items in
   * [[uk.co.randomcoding.partsdb.lift.util.snippet.AllLineItemsSnippet#lineItemHolder]] with
   * [[uk.co.randomcoding.partsdb.lift.util.LineItemDisplay]]. This is done via `JsCmds.SetHtml`.
   */
  def refreshLineItemDisplay(): JsCmd = refreshLineItemDisplay(dataHolder.lineItems)

  /**
   * Provides a `CssSel` transformation of the element with the id `lineItems` to contain the results of rendering all the line items in
   * [[uk.co.randomcoding.partsdb.lift.util.snippet.AllLineItemsSnippet#lineItemHolder]] with
   * [[uk.co.randomcoding.partsdb.lift.util.LineItemDisplay]]
   */
  def renderAllLineItems(): CssSel = renderAllLineItems(dataHolder.lineItems)

  /**
   * Updates the content of the element with the id `lineItems` to contain the results of rendering all the line items in the `lineItems` with
   * [[uk.co.randomcoding.partsdb.lift.util.LineItemDisplay]]. This is done via `JsCmds.SetHtml`.
   */
  def refreshLineItemDisplay(lineItems: Seq[LineItem]): JsCmd = {
    debug("Updating Line Items Display with: %s".format(lineItems.mkString("[", ", ", "]")))
    Replace("lineItems", wrapInIdSpan(LineItemDisplay(lineItems, false, false), "lineItems"))
  }

  /**
   * Provides a `CssSel` transformation of the element with the id `lineItems` to contain the results of rendering all the line items in the `lineItems` with
   * [[uk.co.randomcoding.partsdb.lift.util.LineItemDisplay]]
   */
  def renderAllLineItems(lineItems: Seq[LineItem]): CssSel = "#lineItems" #> wrapInIdSpan(LineItemDisplay(lineItems, false, false), "lineItems")

  private[this] def wrapInIdSpan(content: NodeSeq, id: String): NodeSeq = <span>{ content }</span> % Attribute(None, "id", Text("lineItems"), Null)
}
