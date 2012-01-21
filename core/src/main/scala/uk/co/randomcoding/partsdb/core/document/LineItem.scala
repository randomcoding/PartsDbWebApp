/**
 *
 */
package uk.co.randomcoding.partsdb.core.document
import uk.co.randomcoding.partsdb.core.id.Identifier

/**
 * A line item for documents.
 *
 * @constructor Create a new line item instance
 * @param lineNumber The index of the line
 * @param partId The [[uk.co.randomcoding.partsdb.core.id.Identifier]] of the [[uk.co.randomcoding.partsdb.core.part.Part]] that this line item is selling
 * @param quantity The number of parts in this line item
 * @param basePrice The base price of the part being sold. This is derived from the [[uk.co.randomcoding.partsdb.core.supplier.Supplier]]'s price for the part
 * @param markup The percentage markup to apply to the base price. This derives the total, pre tax, price per part for this line item.
 * This is usually in the range 0.0 to 1.0 where 1.0 is a 100% markup.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
case class LineItem(lineNumber: Int, partId: Identifier, quantity: Int, basePrice: Double, markup: Double) {

  /**
   * Calculates the cost of the line item.
   *
   * This is done by
   * {{{
   * (basePrice + (basePrice * markup)) * quantity
   * }}}
   *
   * @return A double value which is
   */
  def lineCost: Double = (basePrice + (basePrice * markup)) * quantity
}