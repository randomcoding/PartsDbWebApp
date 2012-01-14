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

  def addNewPart(partName: String, cost: Double, vehicle: Vehicle): Part = {
    // check parts are in db or not and assign/get their Ids 
    // for now assume parts are new and assign them ids
    val part = assignId(Part(-1L, partName, cost, Some(vehicle))).asInstanceOf[Part]
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

  def editPart(partId: Identifier, partName: String, cost: Double, vehicle: Vehicle): Part = {
    val part = Part(partId, partName, cost, Some(vehicle))
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

  def addNewVehicle(vehicleName: String): Vehicle = {
    // check vehicles are in db or not and assign/get their Ids 
    // for now assume vehicles are new and assign them ids
    val vehicle = assignId(Vehicle(-1L, vehicleName)).asInstanceOf[Vehicle]
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

  def getAllVehicles(): List[Vehicle] = getAll[Vehicle]("vehicleId")

  def addQuote(newQuote: Document): Document = {
    val quote = assignId(newQuote).asInstanceOf[Document]
    add(quote) match {
      case true => info("Added Quote: %s".format(quote))
      case false => error("Failed to add Quote: %s".format(quote))
    }
    quote
  }
}
