/**
 *
 */
package uk.co.randomcoding.partsdb.core.part

import net.liftweb.mongodb.record.field._
import net.liftweb.mongodb.record.{ MongoRecord, MongoMetaRecord }
import net.liftweb.record.field.DoubleField
import org.joda.time.DateTime
import org.bson.types.ObjectId
import com.foursquare.rogue.Rogue._
import java.util.Date

/**
 * Provides a mapping for [[uk.co.randomcoding.partsdb.core.supplier.Supplier]]s to the
 * [[uk.co.randomcoding.partsdb.core.part.Part]]s they supply, the cost of the [[uk.co.randomcoding.partsdb.core.part.Part]]
 * and the last time it was purchased from them.
 *
 * @author Jane Rowe
 * @author RandomCoder - Changed to MongoRecord class
 */
class PartCost private () extends MongoRecord[PartCost] with ObjectIdPk[PartCost] {
  def meta = PartCost

  object part extends ObjectIdRefField(this, Part)
  object suppliedCost extends DoubleField(this)
  object lastSuppliedDate extends DateField(this)

  override def equals(that: Any): Boolean = {
    that match {
      case pc: PartCost => part.get == pc.part.get && suppliedCost.get == pc.suppliedCost.get
      case _ => false
    }
  }

  override def hashCode: Int = getClass.hashCode + part.get.hashCode + suppliedCost.get.hashCode
}

object PartCost extends PartCost with MongoMetaRecord[PartCost] {

  /**
   * Creates a new `PartCost` record but '''does not''' commit it to the database
   */
  def create(part: Part, cost: Double, lastSupplied: DateTime): PartCost = {
    PartCost.createRecord.part(part.id.get).suppliedCost(cost).lastSuppliedDate(lastSupplied.toDate)
  }

  /**
   * Creates a new `PartCost` and adds it to the database.
   *
   * This will add the part to the database if if is not already present
   *
   * @return A full `Option[PartCost]` if the db add was k, `None` otherwise
   */
  def add(part: Part, cost: Double, lastSupplied: DateTime): Option[PartCost] = {
    val partToAdd = Part.findMatching(part) match {
      case None => Part.add(part)
      case Some(p) => Some(p)
    }

    require(partToAdd isDefined, "Failed to find or create the part record. Not adding Part Cost")
    add(create(partToAdd get, cost, lastSupplied))
  }

  /**
   * Creates a new `PartCost` and adds it to the database.
   *
   * This will '''not''' be able to add a new Part to the database if the referenced part does not exist
   *
   * @return A full `Option[PartCost]` if the db add was ok, `None` otherwise
   */
  def add(partCost: PartCost): Option[PartCost] = findMatching(partCost) match {
    case Some(pc) => Some(pc)
    case _ => partCost save match {
      case p: PartCost => Some(p)
      case _ => None
    }
  }

  /**
   * Find the `PartCost` with the given Object Id.
   *
   * @return A populated `Option[PartCost]` if there is a match in the database, `None` otherwise
   */
  def findById(oid: ObjectId): Option[PartCost] = PartCost where (_.id eqs oid) get

  /**
   * Find all that `PartCost`s that are for the given part
   */
  def findByPart(part: Part): List[PartCost] = PartCost where (_.part eqs part.id.get) fetch

  /**
   * Find a record that matches the provided one.
   *
   * If there is a record with a matching `id` then that is returned, otherwise the ''first''
   * record that has the same part id and supplied cost is returned.
   */
  def findMatching(partCost: PartCost): Option[PartCost] = findById(partCost.id.get) match {
    case Some(pc) => Some(pc)
    case _ => {
      PartCost where (_.part eqs partCost.part.get) and (_.suppliedCost between (partCost.suppliedCost.get - 0.01, partCost.suppliedCost.get + 0.01)) get
    }
  }

  def modify(oid: ObjectId, partId: ObjectId, cost: Double, lastSupplied: Date): Unit = {
    PartCost.where(_.id eqs oid).modify(_.part setTo partId) and (_.suppliedCost setTo cost) and (_.lastSuppliedDate setTo lastSupplied) updateMulti
  }

  def modify(oid: ObjectId, newPartCost: PartCost): Unit = {
    modify(oid, newPartCost.part.get, newPartCost.suppliedCost.get, newPartCost.lastSuppliedDate.get)
  }

  /**
   * Remove the `PartCost` with the given object id
   */
  def remove(oid: ObjectId) = PartCost where (_.id eqs oid) bulkDelete_!!
}