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

import com.foursquare.rogue.Rogue._

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
  object modId extends OptionalStringField(this, 50)

  override def equals(that: Any): Boolean = {
    that.isInstanceOf[Part] match {
      case true => {
        val other = that.asInstanceOf[Part]
        partName.get == other.partName.get && vehicle.get == other.vehicle.get && modId.get == other.modId.get
      }
      case false => false
    }
  }

  override def hashCode: Int = {
    var hash = getClass.hashCode
    hash += partName.get.hashCode + vehicle.get.hashCode + modId.get.hashCode

    hash
  }
}

object Part extends Part with MongoMetaRecord[Part] {

  import org.bson.types.ObjectId
  
  /**
   * Find a part by its object id
   * 
   * @return An optional part containing the `Part` with the given id if it is found or `None` if it is not
   */
  def findById(id: ObjectId): Option[Part] = Part where (_.id eqs id) get
  
  /**
   * Find all parts with the given name in the database.
   *
   * This '''should''' always return at most a single record.
   */
  def findNamed(name: String): List[Part] = Part where (_.partName eqs name) fetch

  /**
   * Add a new part to the database, unless there is a part with the same name already present
   */
  def add(name: String, vehicle: Vehicle, modId: Option[String] = None) = {
    findNamed(name) match {
      case Nil => {
        Part.createRecord.partName(name).vehicle(vehicle.id.get).modId(modId).save match {
          case p: Part => Some(p)
          case _ => None
        }
      }
      case _ => None
    }
  }

  /**
   * Remove all entries with the given part name from the database
   *
   *  This '''should''' only affect a single record.
   */
  def remove(name: String) = findNamed(name) map (_.delete_!) distinct

  /**
   * Update the part name, vehicle type and modId for '''ALL''' parts with the name `oldName` in the database.
   *
   *  This '''should''' only affect a single record however.
   */
  def modify(oldName: String, newName: String, newVehicle: Vehicle, newModId: Option[String] = None) = {
    Part where (_.partName eqs oldName) modify (_.partName setTo newName) and (_.vehicle setTo newVehicle.id.get) and (_.modId setTo newModId.getOrElse("")) updateMulti
  }
}

