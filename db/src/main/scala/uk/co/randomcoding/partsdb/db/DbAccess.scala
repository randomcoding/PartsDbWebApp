/**
 *
 */
package uk.co.randomcoding.partsdb.db

import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.contact.ContactDetails
import uk.co.randomcoding.partsdb.core.customer.{ DefaultCustomer, Customer }
import uk.co.randomcoding.partsdb.core.document.{ LineItem, DocumentType, Document }
import uk.co.randomcoding.partsdb.core.id.Identifier._
import uk.co.randomcoding.partsdb.core.id.{ Identifier, DefaultIdentifier }
import uk.co.randomcoding.partsdb.core.part.{ Part, DefaultPart }
import uk.co.randomcoding.partsdb.core.terms.PaymentTerms
import uk.co.randomcoding.partsdb.core.vehicle.{ Vehicle, DefaultVehicle }
import uk.co.randomcoding.partsdb.db.mongo.{ MongoUpdateAccess, MongoIdentifierAccess, MongoConfig, MongoAllOrOneAccess }
import net.liftweb.common.Logger
import uk.co.randomcoding.partsdb.core.part.{ PartCost, DefaultPartCost }
import uk.co.randomcoding.partsdb.core.part.{ PartKit, DefaultPartKit }
import java.util.Date

/**
 * Encapsulates all the Database access functionality in a single class
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 * @author Jane Rowe
 */
trait DbAccess extends MongoIdentifierAccess with MongoUpdateAccess with MongoAllOrOneAccess with Logger {

  /**
   * @param dbName The name of the database to connect to. Defaults to ''MainDb''
   */
  val dbName: String = "MainDb"

  /**
   * @param collectionName The name of the Collection to get from the database. Defaults to ''MainCollection''
   */
  val collectionName: String = "MainCollection"

  override lazy val collection = MongoConfig.getCollection(dbName, collectionName)

  def addNewCustomer(contactName: String, billingAddress: Address, terms: PaymentTerms, contact: ContactDetails): Customer = {
    // check addresses is are in db or not and assign/get their Ids 
    // for now assume addresses are new and assign them ids
    // FIXME - The cast to Address is nasty and hacky
    val bAddr = assignId(billingAddress).asInstanceOf[Address]
    debug("Billing Address (with id): %s".format(bAddr))
    add(bAddr)
    val customer = assignId(Customer(-1L, contactName, bAddr.addressId, terms, contact)).asInstanceOf[Customer]
    debug("Updating database with customer %s at billing address: %s".format(customer, billingAddress))
    add(customer) match {
      case true => {
        debug("Added new customer %s with billing address %s".format(customer, bAddr))
        customer
      }
      case false => {
        error("Failed to add customer %s with billing address %s".format(customer, bAddr))
        DefaultCustomer
      }
    }
  }

  def addNewPart(partName: String, vehicles: List[Vehicle], modId: String): Part = {
    // TODO check part is in db or not, and assign/get the id. For now assume part is new and assign the id
    val part = assignId(Part(-1L, partName, Some(vehicles), Some(modId))).asInstanceOf[Part]
    debug("Updating database with part %s".format(part))
    add(part) match {
      case true => {
        debug("Added new part %s".format(part))
        part
      }
      case false => {
        error("Failed to add part %s".format(part))
        DefaultPart
      }
    }
  }

  def editPart(partId: Identifier, partName: String, vehicles: List[Vehicle], modId: String): Part = {
    // TODO get part from db for editing, add error checking if part is not in the db
    val part = Part(partId, partName, Some(vehicles), Some(modId))
    debug("Updating database with part %s".format(part))
    modify(part) match {
      case true => {
        debug("Modified new part %s".format(part))
        part
      }
      case false => {
        error("Failed to modify part %s".format(part))
        DefaultPart
      }
    }
  }

  def addNewPartKit(kitName: String, parts: List[Part]): PartKit = {
    // TODO check part kit is in db or not, and assign/get the id. For now assume part kit is new and assign the id
    val partKit = assignId(PartKit(-1L, kitName, parts)).asInstanceOf[PartKit]
    debug("Updating database with partKit %s".format(partKit))
    add(partKit) match {
      case true => {
        debug("Added new part %s".format(partKit))
        partKit
      }
      case false => {
        error("Failed to add partKit %s".format(partKit))
        DefaultPartKit
      }
    }
  }

  def editPartKit(kitId: Identifier, kitName: String, parts: List[Part]): PartKit = {
    // TODO get partKit from db for editing, add error checking if partKit is not in the db
    val partKit = PartKit(kitId, kitName, parts)
    debug("Updating database with part %s".format(partKit))
    modify(partKit) match {
      case true => {
        debug("Modified new partKit %s".format(partKit))
        partKit
      }
      case false => {
        error("Failed to modify partKit %s".format(partKit))
        DefaultPartKit
      }
    }
  }

  def addNewPartCost(partId: Identifier, supplierPartId: String, suppliedCost: Double, lastSuppliedDate: Date): PartCost = {
    // TODO check partCost is in db or not, and assign/get the id. For now assume partCost is new and assign the id
    val partCost = assignId(PartCost(-1L, partId, supplierPartId, suppliedCost, lastSuppliedDate)).asInstanceOf[PartCost]
    debug("Updating database with partCost %s".format(partCost))
    add(partCost) match {
      case true => {
        debug("Added new partCost %s".format(partCost))
        partCost
      }
      case false => {
        error("Failed to add partCost %s".format(partCost))
        DefaultPartCost
      }
    }
  }

  def editPartCost(partCostId: Identifier, partId: Identifier, supplierPartId: String, suppliedCost: Double, lastSuppliedDate: Date): PartCost = {
    // TODO get partCost from db for editing, add error checking if partCost is not in the db
    val partCost = PartCost(partCostId, partId, supplierPartId, suppliedCost, lastSuppliedDate)
    debug("Updating database with partCost %s".format(partCost))
    modify(partCost) match {
      case true => {
        debug("Modified new partCost %s".format(partCost))
        partCost
      }
      case false => {
        error("Failed to modify partCost %s".format(partCost))
        DefaultPartCost
      }
    }
  }

  def addNewVehicle(vehicleName: String, vehicleManual: String): Vehicle = {
    // TODO check vehicle is in db or not, and assign/get the id. For now assume vehicle is new and assign the id
    val vehicle = assignId(Vehicle(-1L, vehicleName, vehicleManual)).asInstanceOf[Vehicle]
    debug("Updating database with vehicle %s".format(vehicle))
    add(vehicle) match {
      case true => {
        debug("Added new vehicle %s".format(vehicle))
        vehicle
      }
      case false => {
        error("Failed to add vehicle %s".format(vehicle))
        DefaultVehicle
      }
    }
  }

  def editVehicle(vehicleId: Identifier, vehicleName: String, vehicleManual: String): Vehicle = {
    // TODO get vehicle from db for editing, add error checking if vehicle is not in the db
    val vehicle = assignId(Vehicle(vehicleId, vehicleName, vehicleManual)).asInstanceOf[Vehicle]
    debug("Updating database with vehicle %s".format(vehicle))
    add(vehicle) match {
      case true => {
        debug("Edited vehicle %s".format(vehicle))
        vehicle
      }
      case false => {
        error("Failed to edit vehicle %s".format(vehicle))
        DefaultVehicle
      }
    }
  }

  def getAllVehicles(): List[Vehicle] = getAll[Vehicle]("vehicleId")

  def addQuote(lineItems: List[LineItem]): Document = {
    val quote = assignId(Document(DefaultIdentifier, DocumentType.Quote, lineItems)).asInstanceOf[Document]
    add(quote) match {
      case true => info("Added Quote: %s".format(quote))
      case false => error("Failed to add Quote: %s".format(quote))
    }
    quote
  }
}
