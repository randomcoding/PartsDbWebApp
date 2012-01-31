/**
 *
 */
package uk.co.randomcoding.partsdb.core.part

import uk.co.randomcoding.partsdb.core.id.{ Identifier, Identifiable, DefaultIdentifier }
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle

/**
 * The data object for a Part.
 *
 * @constructor Create a new part object
 * @param partId The [[uk.co.randomcoding.partsdb.core.id.Identifier]] of this part. This is used for internal referencing of part objects from other entities.
 * @param partName The short (friendly) name of this part
 * @param vehicles A List of [[uk.co.randomcoding.partsdb.core.vehicle.Vehicle]] that can use this part
 * @param supplied A List of a tuple of [[uk.co.randomcoding.partsdb.core.supplier.Supplier, cost]] that covers the supplier with the costs
 *
 * @param partIdMod The MoD identification value for this part
 *
 * val vehicles: Option[List[Vehicle]] = None
 *
 * @author Jane Rowe
 *
 */
case class Part(val partId: Identifier, val partName: String, val vehicles: Option[List[Vehicle]] = None, val modId: Option[String] = None) extends Identifiable {
  override val identifierFieldName = "partId"
}

object DefaultPart extends Part(DefaultIdentifier, "No Part")

