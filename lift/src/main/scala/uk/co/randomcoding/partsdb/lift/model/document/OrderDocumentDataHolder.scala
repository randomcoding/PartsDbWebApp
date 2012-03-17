/**
 *
 */
package uk.co.randomcoding.partsdb.lift.model.document

import net.liftweb.util.Cell

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class OrderDocumentDataHolder extends DocumentDataHolder with LineItemsDataHolder {
  /**
   * The total computed base cost of the line items, before tax
   */
  override val preTaxTotal: Cell[Double] = itemsPreTaxSubTotal.lift(carriageCell)(_ + _)

  override val lineItemsSubTotalCell: Cell[Double] = itemsPreTaxSubTotal.lift(_ + 0.0d)
}