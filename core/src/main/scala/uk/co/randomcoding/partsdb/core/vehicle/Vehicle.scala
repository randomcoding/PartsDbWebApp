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

  /**
   * The name of the vehicle, limited to 50 characters
   */
  object vehicleName extends StringField(this, 50)

  /**
   * The name of the pdf file that contains the details of this vehicle.
   *
   * This should be the file name '''only''' as this will be appended to the common path from
   * [[uk.co.randomcoding.partsdb.core.system.SystemData]]
   */
  object pdfFile extends StringField(this, 50)

  override def equals(that: Any): Boolean = that.isInstanceOf[Vehicle] && that.asInstanceOf[Vehicle].vehicleName.get == vehicleName.get

  override def hashCode: Int = getClass.hashCode + vehicleName.get.hashCode
}

object Vehicle extends Vehicle with MongoMetaRecord[Vehicle] {

  import org.bson.types.ObjectId

  /**
   * Find a vehicle with a given object id
   */
  def findById(objectId: ObjectId): Option[Vehicle] = Vehicle where (_.id eqs objectId) get

  /**
   * Find all vehicles with a given name.
   *
   * This '''should''' only return a single element
   */
  def findNamed(name: String): List[Vehicle] = Vehicle where (_.vehicleName eqs name) fetch

  def findMatching(vehicle: Vehicle): Option[Vehicle] = findById(vehicle.id.get) match {
    case Some(v) => Some(v)
    case _ => findNamed(vehicle.vehicleName.get) headOption
  }

  def add(vehicle: Vehicle): Option[Vehicle] = findMatching(vehicle) match {
    case Some(v) => Some(v)
    case _ => vehicle.save match {
      case v: Vehicle => Some(v)
      case _ => None
    }
  }
  /**
   * Add a new vehicle, if one does not already exist with the same name
   */
  def add(name: String): Option[Vehicle] = add(create(name))

  /**
   * Create a record but '''does not''' add it to the database
   */
  def create(vehicleName: String, pdfFile: String = ""): Vehicle = Vehicle.createRecord.vehicleName(vehicleName).pdfFile(pdfFile)

  /**
   * Rename all vehicles with `oldName` to `newName`.
   *
   * This '''should''' only affect a single record.
   */
  def modify(oldName: String, newName: String, newPdfFile: String) {
    Vehicle where (_.vehicleName eqs oldName) modify (_.vehicleName setTo newName) modify (_.pdfFile setTo newPdfFile) updateMulti
  }

  /**
   * Remove all records found with the given name.
   *
   * This '''should''' only affect a single record.
   */
  def remove(name: String) = findNamed(name) map (_.delete_!) distinct
}

