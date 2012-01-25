/**
 *
 */
package uk.co.randomcoding.partsdb.core.vehicle

import uk.co.randomcoding.partsdb.core.id.{ Identifier, Identifiable }
import uk.co.randomcoding.partsdb.core.id.DefaultIdentifier
import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.record.field.StringField

/**
 * @constructor Create a new vehicle object
 * @param vehicleId The [[uk.co.randomcoding.partsdb.core.id.Identifier]] of this vehicle. This is used for internal referencing of vehicle objects from other entities.
 * @param vehicleName The short (friendly) name of this vehicle
 *
 * @author Jane Rowe
 * @author RandomCoder - Changed to MongoRecord class
 *
 */
class Vehicle extends MongoRecord[Vehicle] with ObjectIdPk[Vehicle] {
  def meta = Vehicle

  object vehicleName extends StringField(this, 50)
}

object Vehicle extends Vehicle with MongoMetaRecord[Vehicle]

/*case class Vehicle(val vehicleId: Identifier, val vehicleName: String) extends Identifiable {
  override val identifierFieldName = "vehicleId"
}

object DefaultVehicle extends Vehicle(DefaultIdentifier, "Default Vehicle")*/
