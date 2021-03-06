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
import uk.co.randomcoding.partsdb.core.document.{ LineItem, Document }
import uk.co.randomcoding.partsdb.lift.util.SnippetDisplayHelpers._
import net.liftweb.util.Helpers._
import net.liftweb.util.CssSel
import net.liftweb.http.js.JsCmds.SetHtml
import scala.xml.Attribute
import scala.xml.Null
import scala.xml.NodeSeq
import uk.co.randomcoding.partsdb.core.system.SystemData

/**
 * Display the totals (subtotal, carriage, vat and grant total) of a [[uk.co.randomcoding.partsdb.core.document.Document]]
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait DocumentTotalsDisplay {
  /**
   * Render the actual values from the provided [[uk.co.randomcoding.partsdb.core.document.Document]]
   *
   * Expects the elements with the following ids
   * - ''subtotal'' - Displays the pre-tax total of all the goods in all [[uk.co.randomcoding.partsdb.core.document.LineItem]]s from the [[uk.co.randomcoding.partsdb.core.document.Document]]
   * - ''carriage'' - Displays the carriage value from the [[uk.co.randomcoding.partsdb.core.document.Document]]
   * - ''vat'' - Displays the VAT amount for the ''subtotal'' and ''carriage''
   * - ''total'' The total of the ''subtotal'', ''carriage'' and ''vat'' fields. This is calculated
   */
  def renderDocumentTotals(document: Document): CssSel = {
    "#subtotal *" #> renderSubtotal(document.lineItems.get) &
      "#carriage *" #> Text(currencyFormat(document.carriage.get)) &
      "#vat *" #> renderVat(document) &
      "#total *" #> renderTotal(document)
  }

  private[this] def renderSubtotal(lineItems: Seq[LineItem]) = renderIdSpan(Text(currencyFormat(lineItemSubTotal(lineItems))), "subtotal")

  private[this] def renderVat(document: Document) = renderIdSpan(Text(currencyFormat(document.documentVat)), "vat")

  private[this] def renderTotal(document: Document) = renderIdSpan(Text(currencyFormat(document.documentValue)), "total")

  private[this] def renderIdSpan(content: NodeSeq, id: String) = {
    <span>
      { content }
    </span> % Attribute("id", Text(id), Null)
  }

  def refreshSubtotal(lineItems: Seq[LineItem]) = SetHtml("subtotal", renderSubtotal(lineItems))

  def refreshVat(document: Document) = SetHtml("vat", Text(currencyFormat(document.documentVat)))

  def refreshTotal(document: Document) = SetHtml("total", Text(currencyFormat(document.documentValue)))

  def refreshTotals(document: Document) = refreshSubtotal(document.lineItems.get) & refreshVat(document) & refreshTotal(document)

  private[this] def lineItemSubTotal(lineItems: Seq[LineItem]) = lineItems map (_.lineCost) sum
}
