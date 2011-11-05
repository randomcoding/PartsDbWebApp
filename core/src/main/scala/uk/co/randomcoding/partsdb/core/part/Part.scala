/**
 *
 */
package uk.co.randomcoding.partsdb.core.part

import uk.co.randomcoding.partsdb.core.id.Identifier
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle
/**
 * @constructor Create a new part object
 * @param partId The [[uk.co.randomcoding.partsdb.core.id.Identifier]] of this part. This is used for internal referencing of part objects from other entities.
 * @param partName The short (friendly) name of this part
 * @param vehicles The set of vehicles this part can be used for
 *
 * @author JMRowe <>
 *
 */
case class Part(val partId: Identifier, val partName: String, val vehicles: Set[Vehicle])