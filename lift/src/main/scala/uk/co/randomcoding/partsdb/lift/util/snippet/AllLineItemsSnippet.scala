/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet

import uk.co.randomcoding.partsdb.core.document.LineItem
import uk.co.randomcoding.partsdb.lift.model.document.LineItemsDataHolder
import uk.co.randomcoding.partsdb.lift.util._

import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.http.js.JsCmd
import net.liftweb.util.Helpers._
import net.liftweb.util.CssSel

/**
 * A simple snippet to display all lineitems, either from a [[uk.co.randomcoding.partsdb.lift.model.document.LineItemsDataHolder]] or passed in directly
 * via [[uk.co.randomcoding.partsdb.lift.util.snippet.AllLineItemsSnippet#refreshLineItemDisplay(Seq[LineItem])]] or
 * [[uk.co.randomcoding.partsdb.lift.util.snippet.AllLineItemsSnippet#renderAllLineItems(Seq[LineItem])]]
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait AllLineItemsSnippet {
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
  def refreshLineItemDisplay(lineItems: Seq[LineItem]): JsCmd = SetHtml("lineItems", LineItemDisplay(lineItems, false, false))

  /**
   * Provides a `CssSel` transformation of the element with the id `lineItems` to contain the results of rendering all the line items in the `lineItems` with
   * [[uk.co.randomcoding.partsdb.lift.util.LineItemDisplay]]
   */
  def renderAllLineItems(lineItems: Seq[LineItem]): CssSel = "#lineItems" #> LineItemDisplay(lineItems, false, false)
}