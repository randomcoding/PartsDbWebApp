/**
 *
 */
package uk.co.randomcoding.partsdb.lift.model.document

import net.liftweb.util.Cell
import uk.co.randomcoding.partsdb.core.customer.Customer

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class OrderDocumentDataHolder(cust: Option[Customer]) extends DocumentDataHolder with LineItemsDataHolder {
  customer = cust

  /**
   * The total computed base cost of the line items, before tax
   */
  override val preTaxTotal: Cell[Double] = itemsPreTaxSubTotal.lift(carriageCell)(_ + _)

  override val lineItemsSubTotalCell: Cell[Double] = itemsPreTaxSubTotal.lift(_ + 0.0d)
}