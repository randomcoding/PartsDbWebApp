/**
 *
 */
package uk.co.randomcoding.partsdb.lift.model.document

import net.liftweb.util.Cell
/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class QuoteDocumentDataHolder extends DocumentDataHolder with NewLineItemDataHolder {
  /**
   * The total computed base cost of the line items, before tax
   */
  override val preTaxTotal: Cell[Double] = itemsPreTaxSubTotal.lift(carriageCell)(_ + _)

  val subTotal: Cell[String] = itemsPreTaxSubTotal.lift("£%.2f".format(_))

}