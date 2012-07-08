/*
 * Copyright (C) 2012 RandomCoder <randomcoder@randomcoding.co.uk>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *    RandomCoder - initial API and implementation and/or initial documentation
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
    getClass.hashCode + partName.get.hashCode + vehicle.get.hashCode + modId.get.hashCode
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
   * Find a part that matches the provided one.
   *
   * A match is found if:
   *   - There is a `Part` with the same `object id` in the database
   *   - There is another part that has the same `part name` and `vehicle ref id`
   */
  def findMatching(part: Part): Option[Part] = findById(part.id.get) match {
    case Some(p) => Some(p)
    case _ => Part where (_.partName eqs part.partName.get) and (_.vehicle eqs part.vehicle.get) get
  }

  /**
   * Create a `Part` record instance but '''DO NOT''' commit it to the database
   */
  def create(name: String, vehicle: Vehicle, modId: Option[String] = None): Part = {
    Part.createRecord.partName(name).vehicle(vehicle.id.get).modId(modId)
  }

  /**
   * Add a new part to the database, unless there is a matching part already present
   */
  def add(name: String, vehicle: Vehicle, modId: Option[String] = None): Option[Part] = {
    add(create(name, vehicle, modId))
  }

  /**
   * Add a new part to the database, unless there is a matching part already present
   */
  def add(part: Part): Option[Part] = findMatching(part) match {
    case Some(p) => Some(p)
    case _ => part.save match {
      case p: Part => Some(p)
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
  def modify(oldName: String, newName: String, newVehicle: Vehicle, newModId: Option[String] = None) {
    Part.where(_.partName eqs oldName).modify(_.partName setTo newName) and (_.vehicle setTo newVehicle.id.get) and (_.modId setTo newModId.getOrElse("")) updateMulti
  }
}

