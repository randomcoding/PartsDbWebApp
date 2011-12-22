/**
 *
 */
package uk.co.randomcoding.partsdb.core.part

import uk.co.randomcoding.partsdb.core.id.{ Identifier, Identifiable }
import uk.co.randomcoding.partsdb.core.id.DefaultIdentifier
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle

/**
 * @constructor Create a new part object
 * @param partId The [[uk.co.randomcoding.partsdb.core.id.Identifier]] of this part. This is used for internal referencing of part objects from other entities.
 * @param partName The short (friendly) name of this part
 * @param cost The cost of the part
 *
 * @author Jane Rowe
 *
 */
case class Part(val partId: Identifier, val partName: String, val partCost: Double, val vehicle: Option[Vehicle] = None) extends Identifiable {
  override val identifierFieldName = "partId"
}

object DefaultPart extends Part(DefaultIdentifier, "No Part", 00.00)
