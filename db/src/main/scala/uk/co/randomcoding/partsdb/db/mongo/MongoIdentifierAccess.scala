/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import com.mongodb.casbah.Imports._
import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.id.Identifier._
import uk.co.randomcoding.partsdb.core.id.{ Identifiable, DefaultIdentifier }
import uk.co.randomcoding.partsdb.core.part.Part
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
trait MongoIdentifierAccess {
  val collection: MongoCollection

  private val incrementUniqueId = (currentId: Long) => {
    val newIdObject = MongoDBObject("uniqueId" -> (currentId + 1))
    val findIdObjectQuery = "uniqueId" $exists true
    collection.findOne(findIdObjectQuery) match {
      case None => collection += newIdObject
      case _ => collection.findAndModify(findIdObjectQuery, newIdObject)
    }
  }

  private val idQuery = "uniqueId" $exists true

  /**
   * Gets the next value for the unique id.
   *
   * This also increments the current value that is stored in the database
   */
  def nextId(): Long = {
    val findOneQuery = collection.findOne(idQuery)
    val idValue = findOneQuery match {
      case None => {
        0
      }
      case Some(v) => {
        v.as[Long]("uniqueId")
      }
    }
    incrementUniqueId(idValue)
    idValue
  }

  /**
   * Assigns a valid id to an [[uk.co.randomcoding.partsdb.id.Identifiable]] if its' id value is a [[uk.co.randomcoding.partsdb.id.DefaultIdentifier]]
   *
   * If the [[uk.co.randomcoding.partsdb.id.Identifiable]] has nested items that are themselves [[uk.co.randomcoding.partsdb.id.Identifiable]]s
   * then this will recurse and assign new ids to those items. However, if there are nested items that are [[uk.co.randomcoding.partsdb.id.Identifier]]s
   * then these are assumed to be valid and are '''not''' checked.
   *
   * @param item The [[uk.co.randomcoding.partsdb.id.Identifiable]] to assign a valid id to
   * @return A new instance of the [uk.co.randomcoding.partsdb.id.Identifiable]] with a valid id or the same instance if its current id is not
   * the [[uk.co.randomcoding.partsdb.id.DefaultIdentifier]]
   */
  def assignId(item: Identifiable): Identifiable = {
    val defaultId = (i: Identifiable) => i.id == DefaultIdentifier.id

    item match {
      case cust: Customer if defaultId(cust) => Customer(nextId(), cust.customerName, cust.billingAddress, cust.deliveryAddresses, cust.terms, cust.contactDetails)
      case addr: Address if defaultId(addr) => Address(nextId(), addr.shortName, addr.addressText, addr.country)
      case vehicle: Vehicle if defaultId(vehicle) => Vehicle(nextId(), vehicle.vehicleName)
      case part: Part if defaultId(part) => Part(nextId(), part.partName, part.partCost, part.vehicle)
      case _ => item
    }
  }
}