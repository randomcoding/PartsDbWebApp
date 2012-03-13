/**
 *
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

/**
 * Display the totals (subtotal, carriage, vat and grant total) of a [[uk.co.randomcoding.partsdb.core.document.Document]]
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait DocumentTotalsDisplay {
  private val vatRate = 0.2d

  /**
   * Render the actual values from the provided [[uk.co.randomcoding.partsdb.core.document.Document]]
   *
   * Expects the elements with the following ids
   *   - ''subtotal'' - Displays the pre-tax total of all the goods in all [[uk.co.randomcoding.partsdb.core.document.LineItem]]s from the [[uk.co.randomcoding.partsdb.core.document.Document]]
   *   - ''carriage'' - Displays the carriage value from the [[uk.co.randomcoding.partsdb.core.document.Document]]
   *   - ''vat'' - Displays the VAT amount for the ''subtotal'' and ''carriage''
   *   - ''total'' The total of the ''subtotal'', ''carriage'' and ''vat'' fields. This is calculated
   */
  def renderDocumentTotals(document: Document): CssSel = {
    renderDocumentTotals(document.lineItems.get, document.carriage.get)
  }

  /**
   * Render the actual values from the provided [[uk.co.randomcoding.partsdb.core.document.LineItem]]s and carriage value
   *
   * Expects the elements with the following ids
   *   - ''subtotal'' - Displays the pre-tax total of all the goods in all [[uk.co.randomcoding.partsdb.core.document.LineItem]]s from the [[uk.co.randomcoding.partsdb.core.document.Document]]
   *   - ''carriage'' - Displays the carriage value from the [[uk.co.randomcoding.partsdb.core.document.Document]]
   *   - ''vat'' - Displays the VAT amount for the ''subtotal'' and ''carriage''
   *   - ''total'' The total of the ''subtotal'', ''carriage'' and ''vat'' fields. This is calculated
   */
  def renderDocumentTotals(lineItems: Seq[LineItem], carriage: Double): CssSel = {
    "#subtotal *" #> renderSubtotal(lineItems) &
      "#carriage *" #> Text(currencyFormat(carriage)) &
      "#vat *" #> renderVat(lineItems) &
      "#total *" #> renderTotal(lineItems, carriage)
  }

  private[this] def renderSubtotal(lineItems: Seq[LineItem]) = renderIdSpan(Text(currencyFormat(subTotal(lineItems))), "subtotal")

  private[this] def renderVat(lineItems: Seq[LineItem]) = renderIdSpan(Text(currencyFormat(vatAmount(lineItems))), "vat")

  private[this] def renderTotal(lineItems: Seq[LineItem], carriage: Double) = renderIdSpan(Text(currencyFormat(total(lineItems, carriage))), "total")

  private[this] def renderIdSpan(content: NodeSeq, id: String) = {
    <span>{ content }</span> % Attribute("id", Text(id), Null)
  }

  def refreshSubtotal(lineItems: Seq[LineItem]) = SetHtml("subtotal", renderSubtotal(lineItems))

  def refreshVat(lineItems: Seq[LineItem]) = SetHtml("vat", Text(currencyFormat(vatAmount(lineItems))))

  def refreshTotal(lineItems: Seq[LineItem], carriage: Double) = SetHtml("total", Text(currencyFormat(total(lineItems, carriage))))

  def refreshTotals(lineItems: Seq[LineItem], carriage: Double) = refreshSubtotal(lineItems) & refreshVat(lineItems) & refreshTotal(lineItems, carriage)

  private def subTotal(lineItems: Seq[LineItem]) = lineItems map (_.lineCost) sum

  private def vatAmount(lineItems: Seq[LineItem]) = subTotal(lineItems) * vatRate

  private def total(lineItems: Seq[LineItem], carriage: Double) = subTotal(lineItems) + vatAmount(lineItems) + carriage
}