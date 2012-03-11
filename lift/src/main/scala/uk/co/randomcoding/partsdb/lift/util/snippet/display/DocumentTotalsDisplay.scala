/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet.display

import scala.xml.Text

import uk.co.randomcoding.partsdb.core.document.{ LineItem, Document }

import net.liftweb.util.Helpers._

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
  def renderDocumentTotals(document: Document) = {
    val lineItems = document.lineItems.get
    val carriage = document.carriage.get
    "#subtotal" #> currencyFormat(subTotal(lineItems)) &
      "#carriage" #> Text(currencyFormat(carriage)) &
      "#vat" #> currencyFormat(vatAmount(lineItems)) &
      "#total" #> currencyFormat(subTotal(lineItems) + vatAmount(lineItems) + carriage)
  }

  private def currencyFormat(value: Double): String = "£%02.2f".format(value)

  private def subTotal(lineItems: Seq[LineItem]) = lineItems map (_.lineCost) sum

  private def vatAmount(lineItems: Seq[LineItem]) = subTotal(lineItems) * vatRate
}