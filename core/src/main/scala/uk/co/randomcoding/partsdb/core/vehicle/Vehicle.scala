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
  object pdfFile extends StringField(this, 50) {
    override val defaultValue = ""
  }

  override def equals(that: Any): Boolean = that.isInstanceOf[Vehicle] && that.asInstanceOf[Vehicle].vehicleName.get == vehicleName.get

  override def hashCode: Int = getClass.hashCode + vehicleName.get.hashCode
}

object Vehicle extends Vehicle with MongoMetaRecord[Vehicle] {

  import org.bson.types.ObjectId

  /**
   * Create a record but '''does not''' add it to the database
   */
  def apply(vehicleName: String, pdfFile: String): Vehicle = create(vehicleName, pdfFile)

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

  /**
   * Finds a `Vehicle` record that has the same `vehicleName` as the provided one.
   *
   * @return An optional result containing the matching record or `None` if there was no match
   */
  def findMatching(vehicle: Vehicle): Option[Vehicle] = findById(vehicle.id.get) match {
    case Some(v) => Some(v)
    case _ => findNamed(vehicle.vehicleName.get) headOption
  }

  /**
   * Add a new vehicle to the database unless there is already  one that matches
   *
   * A match is determined by using [[uk.co.randomcoding.partsdb.core.vehicle.Vehicle#findMatching(Vehicle)]]
   * If a match is found the new vehicle is '''not''' added to the database.
   *
   * @return An option with the newly added vehicle if successful, or the matching vehicle if one was found
   */
  def add(vehicle: Vehicle): Option[Vehicle] = findMatching(vehicle) match {
    case Some(v) => Some(v)
    case _ => vehicle.saveTheRecord
  }
  /**
   * Add a new vehicle, if one does not already exist with the same name
   */
  @deprecated("Use add(Vehicle) instead", "0.7")
  def add(name: String): Option[Vehicle] = add(create(name))

  /**
   * Create a record but '''does not''' add it to the database
   */
  def create(vehicleName: String, pdfFile: String = ""): Vehicle = Vehicle.createRecord.vehicleName(vehicleName).pdfFile(pdfFile)

  /**
   * Rename all vehicles with `oldName` to `newName`.
   *
   * This '''should''' only affect a single record.
   *
   * @return The OPtional record of the vehicle with the new name
   */
  def rename(oldName: String, newName: String): Option[Vehicle] = {
    Vehicle where (_.vehicleName eqs oldName) modify (_.vehicleName setTo newName) updateMulti

    Vehicle.where(_.vehicleName eqs newName).get
  }

  /**
   * Update the values of the `Vehicle` with the id of `oid` to have the field values
   * of the `newVehicle`.
   *
   * @return An option of the modified record or `None` if no record with the given `oid` exists`
   */
  def modify(oid: ObjectId, newVehicle: Vehicle): Option[Vehicle] = {
    Vehicle.where(_.id eqs oid) modify (_.vehicleName setTo newVehicle.vehicleName.get) and (_.pdfFile setTo newVehicle.pdfFile.get) updateMulti

    findById(oid)
  }

  /**
   * Remove all records found with the given name.
   *
   * This '''should''' only affect a single record.
   */
  def remove(name: String) = findNamed(name) map (_.delete_!) distinct
}

