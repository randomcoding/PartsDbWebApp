/**
 *
 */
package uk.co.randomcoding.partsdb.core.part

import uk.co.randomcoding.partsdb.core.id.{ Identifier, Identifiable, DefaultIdentifier }
import java.util.Date

/**
 * The data object for a PartCost. PartCost is owned by a [[uk.co.randomcoding.partsdb.core.supplier.Supplier]].
 *
 * @constructor Create a new PartCost object
 * @param partCostid The [[uk.co.randomcoding.partsdb.core.id.Identifier]] of this part cost. This is used for internal referencing of part collection objects from other entities.
 * @param part The [[uk.co.randomcoding.partsdb.core.part.Part]] of this part cost.
 * @param suppliedCost The cost of this part.
 * @param quoteDate The date the quote for this part was made.
 *
 * @author Jane Rowe
 *
 */
case class PartCost(val partCostId: Identifier, val partId: Identifier, val supplierPartId: String, val suppliedCost: Double, val lastSuppliedDate: Date) extends Identifiable {
  override val identifierFieldName = "partCostId"
}

object DefaultPartCost extends PartCost(DefaultIdentifier, DefaultIdentifier, "Default PartCost", 0.00, java.util.Calendar.getInstance().getTime())
