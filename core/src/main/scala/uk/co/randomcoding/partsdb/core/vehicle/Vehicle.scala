/**
 *
 */
package uk.co.randomcoding.partsdb.core.vehicle

import uk.co.randomcoding.partsdb.core.id.{ Identifier, Identifiable }
import uk.co.randomcoding.partsdb.core.id.DefaultIdentifier

/**
 * @constructor Create a new vehicle object
 * @param vehicleId The [[uk.co.randomcoding.partsdb.core.id.Identifier]] of this vehicle. This is used for internal referencing of vehicle objects from other entities.
 * @param vehicleName The short (friendly) name of this vehicle
 *
 * @author Jane Rowe
 *
 */
case class Vehicle(val vehicleId: Identifier, val vehicleName: String) extends Identifiable {
  override val identifierFieldName = "vehicleId"
}

object DefaultVehicle extends Vehicle(DefaultIdentifier, "Default Vehicle")
