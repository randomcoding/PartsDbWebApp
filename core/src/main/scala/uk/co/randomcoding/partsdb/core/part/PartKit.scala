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

import org.bson.types.ObjectId

import com.foursquare.rogue.Rogue._

import uk.co.randomcoding.partsdb.core.document.LineItem

import net.liftweb.mongodb.record.field._
import net.liftweb.mongodb.record.{ MongoRecord, MongoMetaRecord }
import net.liftweb.record.field._

/**
 * A collection of [[uk.co.randomcoding.partsdb.core.part.Part]]s sold together as a kit.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class PartKit private () extends MongoRecord[PartKit] with ObjectIdPk[PartKit] {
  def meta = PartKit

  /**
   * The name of this part kit.
   *
   * Kit names should be unique in the database
   */
  object kitName extends StringField(this, 100)

  /**
   * The line item records that are contained in this `PartKit`.
   */
  object parts extends BsonRecordListField(this, LineItem)

  /**
   * The description of the PartKit
   */
  object description extends StringField(this, 2048)

  /**
   * Get the total cost of all the line items in this `PartKit`
   */
  def kitPrice: Double = parts.get.foldLeft(0.0d)(_ + _.lineCost)

  /**
   * Get the cost price of all the item in this `PartKit`
   *
   * The cost price is the price without any markup.
   */
  def costPrice: Double = parts.get.foldLeft(0.0d)(_ + _.basePrice.get)

  /**
   * Part Kits are equals if their name and parts are equals
   */
  override def equals(that: Any): Boolean = that match {
    case other: PartKit => kitName.get == other.kitName.get && parts.get == other.parts.get
    case _ => false
  }

  override def hashCode: Int = getClass.hashCode + kitName.get.hashCode + parts.get.foldLeft(0)(_ + _.hashCode)
}

object PartKit extends PartKit with MongoMetaRecord[PartKit] {

  /**
   * Create a new instance of a Part Kit.
   *
   * This is '''not''' added to the database
   */
  def apply(kitName: String, partLines: Seq[LineItem], description: String = "") = PartKit.createRecord.kitName(kitName).parts(partLines.toList).description(description)

  /**
   * Create a new instance of a Part Kit.
   *
   * This is '''not''' added to the database
   */
  def create(kitName: String, partLines: Seq[LineItem], description: String = "") = PartKit(kitName, partLines, description)

  /**
   * Find a `PartKit` by its object id.
   *
   * @param oid The Object Id of the `PartKit` to find
   * @return An optional `PartKit` which is populated iff there is a record with the given Object Id. Returned `None` otherwise.
   */
  def findById(oid: ObjectId): Option[PartKit] = PartKit where (_.id eqs oid) get

  /**
   * Find a record that matches the given `PartKit` in the database.
   *
   * This finds the first match in the database only. So if there are multiple matches then the result is non-deterministic.
   *
   * A match is defined by either
   *  - having the same object id
   *  - having the same `kitName`
   *
   * @param partKit The Part Kit to find a matching record for
   * @return An optional `PartKit` which is populated with the if there is another record in the database that
   */
  def findMatching(partKit: PartKit): Option[PartKit] = findById(partKit.id.get) match {
    case Some(pk) => Some(pk)
    case _ => PartKit where (_.kitName eqs partKit.kitName.get) get
  }

  /**
   * Add a part kit to the database unless a matching record is found
   *
   * If there is a matching record in the database (same object id or same `kitName`) then this record is returned and the provided record
   * is '''NOT''' added. If there is no match then the record is added.
   *
   * @param partKit The part kit to add to the database
   * @return An Optional Part Kit that contains either the added record
   */
  def add(partKit: PartKit): Option[PartKit] = findMatching(partKit) match {
    case Some(pk) => Some(pk)
    case _ => partKit.save match {
      case pk: PartKit => Some(pk)
      case _ => None
    }
  }

  /**
   * Update a `PartKit` record with the contents of a new part kit
   *
   * @param oid The `ObjectId` of the `PartKit` record to update. If there is no part with this id nothing is done.
   * @param partKit The `PartKit` object that contains the new vales (`name` and `parts`)
   * @return An optional `PartKit` that will contain the updated record if there was one with the given `ObjectId` or `None` otherwise.
   */
  def update(oid: ObjectId, partKit: PartKit): Option[PartKit] = {
    PartKit.where(_.id eqs oid).modify(_.kitName setTo partKit.kitName.get)
      .and(_.parts setTo partKit.parts.get)
      .and(_.description setTo partKit.description.get).updateMulti

    findById(oid)
  }
}
