/**
 *
 */
package uk.co.randomcoding.partsdb.core.part

import uk.co.randomcoding.partsdb.core.id.{ Identifier, Identifiable }
import uk.co.randomcoding.partsdb.core.id.DefaultIdentifier
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle
import uk.co.randomcoding.partsdb.core.supplier.{ Supplier, DefaultSupplier }
import java.util.Date

/**
 * @constructor Create a new PartCost object
 * @param partCostid The [[uk.co.randomcoding.partsdb.core.id.Identifier]] of this part cost. This is used for internal referencing of part collection objects from other entities.
 * @param part The [[uk.co.randomcoding.partsdb.core.part.Part]] of this part cost.
 * @param supplier The [[uk.co.randomcoding.partsdb.core.supplier.Supplier]] of this part cost.
 * @param quoteDate The date the quote for this part was made.
 *
 * @author Jane Rowe
 *
 */
case class PartCost(val partCostId: Identifier, part: Part, val suppliedCost: Double, val lastSuppliedDate: Date) extends Identifiable {
  override val identifierFieldName = "partCostId"
}

object DefaultPartCost extends PartCost(DefaultIdentifier, DefaultPart, 0.00, java.util.Calendar.getInstance().getTime())

/**
 * @constructor Create a new PartCost object
 * @param partCostid The [[uk.co.randomcoding.partsdb.core.id.Identifier]] of this part cost. This is used for internal referencing of part collection objects from other entities.
 * @param part The [[uk.co.randomcoding.partsdb.core.part.Part]] of this part cost.
 * @param supplier The [[uk.co.randomcoding.partsdb.core.supplier.Supplier]] of this part cost.
 * @param quoteDate The date the quote for this part was made.
 *
 * @author Jane Rowe
 *
 */
//case class PartCost(val partCostId: Identifier, val part: Part, val supplierId: String, val supplier: Supplier, val quoteDate: Date) extends Identifiable {
//  override val identifierFieldName = "partCostId"
//}
//
//object DefaultPartCost extends PartCost(DefaultIdentifier, DefaultPart, "No Supplier ID", DefaultSupplier, java.util.Calendar.getInstance().getTime())