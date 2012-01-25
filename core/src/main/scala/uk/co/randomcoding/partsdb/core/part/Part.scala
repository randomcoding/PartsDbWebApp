/**
 *
 */
package uk.co.randomcoding.partsdb.core.part

import uk.co.randomcoding.partsdb.core._
import vehicle.Vehicle
import supplier.Supplier

import java.util.Date

import net.liftweb.mongodb.record.{ MongoRecord, MongoMetaRecord }
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._

/**
 * A part for a vehicle.
 *
 * @author Jane Rowe
 * @author RandomCoder - Changed to MongoRecord class
 */
class Part private () extends MongoRecord[Part] with ObjectIdPk[Part] {
  def meta = Part

  object partName extends StringField(this, 50)
  object vehicle extends ObjectIdRefField(this, Vehicle)
  object modIf extends OptionalStringField(this, 50)
}

object Part extends Part with MongoMetaRecord[Part]

