/**
 *
 */
package uk.co.randomcoding.partsdb.core.vehicle

import uk.co.randomcoding.partsdb.core.id.Identifier

/**
 * @constructor Create a new vehicle object
 * @param vehicleId The [[uk.co.randomcoding.partsdb.core.id.Identifier]] of this vehicle. This is used for internal referencing of vehicle objects from other entities.
 * @param vehicleName The short (friendly) name of this vehicle
 *
 * @author JMRowe <>
 *
 */
case class Vehicle(val vehicleId: Identifier, val vehicleName: String)