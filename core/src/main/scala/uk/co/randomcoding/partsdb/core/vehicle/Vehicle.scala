/**
 *
 */
package uk.co.randomcoding.partsdb.core.vehicle

import net.liftweb.mongodb.record.{ MongoRecord, MongoMetaRecord }
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.record.field.StringField

/**
 * A Vehicle.
 *
 * Currently very simple, with just a name. Required for the [[uk.co.randomcoding.partsdb.core.part.Part]] class
 *
 * @author Jane Rowe
 * @author RandomCoder - Changed to MongoRecord class
 *
 */
class Vehicle private () extends MongoRecord[Vehicle] with ObjectIdPk[Vehicle] {
  def meta = Vehicle

  object vehicleName extends StringField(this, 50)
}

object Vehicle extends Vehicle with MongoMetaRecord[Vehicle]

