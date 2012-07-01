/**
 *
 */
package uk.co.randomcoding.partsdb.lift.model.document

import net.liftweb.util.Cell
import uk.co.randomcoding.partsdb.core.customer.Customer

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class OrderDocumentDataHolder(customer: Option[Customer]) extends DocumentDataHolder with LineItemsDataHolder {
  customerCell.set(customer)

  /**
   * The total computed base cost of the line items, before tax
   */
  override val preTaxTotal: Cell[Double] = itemsPreTaxSubTotal.lift(carriageCell)(_ + _)

  override val lineItemsSubTotalCell: Cell[Double] = itemsPreTaxSubTotal.lift(_ + 0.0d)
}