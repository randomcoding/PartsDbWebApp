/**
 *
 */
package uk.co.randomcoding.partsdb.core.vehicle

import uk.co.randomcoding.partsdb.core.id.{ Identifier, Identifiable, DefaultIdentifier }

/**
 * The data object for a Vehicle.
 *
 * @constructor Create a new vehicle object
 * @param vehicleId The [[uk.co.randomcoding.partsdb.core.id.Identifier]] of this vehicle. This is used for internal referencing of vehicle objects from other entities.
 * @param vehicleName The short (friendly) name of this vehicle
 *
 * @author Jane Rowe
 *
 */
case class Vehicle(val vehicleId: Identifier, val vehicleName: String, val vehicleManual: String) extends Identifiable {
  override val identifierFieldName = "vehicleId"
}

object DefaultVehicle extends Vehicle(DefaultIdentifier, "Default Vehicle", "No Manual")
