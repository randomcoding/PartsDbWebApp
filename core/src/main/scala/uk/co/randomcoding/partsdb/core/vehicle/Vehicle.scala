/**
 *
 */
package uk.co.randomcoding.partsdb.core.vehicle

import net.liftweb.mongodb.record.{ MongoRecord, MongoMetaRecord }
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.record.field.StringField
import com.foursquare.rogue.Rogue._

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

  override def equals(that: Any): Boolean = that.isInstanceOf[Vehicle] && that.asInstanceOf[Vehicle].vehicleName.get == vehicleName.get

  override def hashCode: Int = getClass.hashCode + vehicleName.get.hashCode
}

object Vehicle extends Vehicle with MongoMetaRecord[Vehicle] {

  /**
   * Find all vehicles with a given name.
   *
   * This '''should''' only return a single element
   */
  def findNamed(name: String): List[Vehicle] = Vehicle where (_.vehicleName eqs name) fetch

  /**
   * Add a new vehicle, if one does not already exist with the same name
   */
  def add(name: String): Option[Vehicle] = findNamed(name) match {
    case Nil => {
      Vehicle.createRecord.vehicleName(name).save match {
        case v: Vehicle => Some(v)
        case _ => None
      }
    }
    case _ => None
  }

  /**
   * Rename all vehicles with `oldName` to `newName`.
   *
   * This '''should''' only affect a single record.
   */
  def rename(oldName: String, newName: String) = Vehicle where (_.vehicleName eqs oldName) modify (_.vehicleName setTo newName) updateMulti

  /**
   * Remove all records found with the given name.
   *
   * This '''should''' only affect a single record.
   */
  def remove(name: String) = findNamed(name) map (_.delete_!) distinct
}

