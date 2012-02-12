/**
 *
 */
package uk.co.randomcoding.partsdb.db

import uk.co.randomcoding.partsdb.core._
import address.Address
import contact.ContactDetails
import customer.Customer
import document.{ LineItem, Document }
import id.Identifier
import part.Part
import terms.PaymentTerms
import transaction.Transaction
import vehicle.Vehicle

import net.liftweb.common.Logger

/**
 * Encapsulates all the Database access functionality in a single class
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 * @author Jane Rowe
 */
@deprecated("With new MongoDB api, this is to been removed", "0.1")
trait DbAccess extends Logger {

  /**
   * @param dbName The name of the database to connect to. Defaults to ''MainDb''
   */
  val dbName: String = "MainDb"

  /**
   * @param collectionName The name of the Collection to get from the database. Defaults to ''MainCollection''
   */
  /*  val collectionName: String = "MainCollection"

  override lazy val collection = MongoConfig.getCollection(dbName, collectionName)*/

  /**
   * Adds a new [[uk.co.randomcoding.partsdb.core.customer.Customer]] to the database.
   *
   * @param contactName The name of the contact with the Customer
   * @param billingAddress The main address for the customer
   * @param terms The payment terms for this customer
   * @param contact The contact details for this customer
   * @return An optional [[uk.co.randomcoding.partsdb.core.customer.Customer]] if the addition was successful, or `None` if it failed.
   */
  def addNewCustomer(contactName: String, billingAddress: Address, terms: PaymentTerms, contact: ContactDetails): Option[Customer] = None /*= {
    // check addresses is are in db or not and assign/get their Ids 
    // for now assume addresses are new and assign them ids
    // FIXME - The cast to Address is nasty and hacky
    val bAddr = assignId(billingAddress).asInstanceOf[Address]
    debug("Billing Address (with id): %s".format(bAddr))
    add(bAddr)
    val customer = assignId(Customer(DefaultIdentifier, contactName, bAddr.addressId, terms, contact)).asInstanceOf[Customer]
    debug("Updating database with customer %s at billing address: %s".format(customer, billingAddress))
    add(customer) match {
      case true => {
        debug("Added new customer %s with billing address %s".format(customer, bAddr))
        Some(customer)
      }
      case false => {
        error("Failed to add customer %s with billing address %s".format(customer, bAddr))
        //DefaultCustomer
        None
      }
    }
  }*/

  def addNewPart(partName: String, vehicles: Vehicle, modId: String): Option[Part] = None /*= {
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
  }*/

  def editPart(partId: Identifier, partName: String, vehicles: Vehicle, modId: String): Option[Part] = None /*= {
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
  }*/

  def addNewVehicle(vehicleName: String): Option[Vehicle] = None /*= {
    // TODO check vehicle is in db or not, and assign/get the id. For now assume vehicle is new and assign the id
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
  }*/

  def editVehicle(vehicleId: Identifier, vehicleName: String): Option[Vehicle] = None /*= {
    // TODO get vehicle from db for editing, add error checking if vehicle is not in the db
    val vehicle = assignId(Vehicle(vehicleId, vehicleName)).asInstanceOf[Vehicle]
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
  }*/

  //def getAllVehicles(): List[Vehicle] = getAll[Vehicle]("vehicleId")

  /**
   * Add a Quote to the database.
   *
   * This will also create the [[uk.co.randomcoding.partsdb.core.transaction.Transaction]] that 'contains' the Quote.
   *
   * The [[uk.co.randomcoding.partsdb.core.transaction.Transaction]] is added first and if successful, then the
   * [[uk.co.randomcoding.partsdb.core.document.Document]] is added.
   *
   * @param newQuote The new quote [[uk.co.randomcoding.partsdb.core.document.Document]] to add.
   * If this has a [[uk.co.randomcoding.partsdb.core.identifier.DefaultIdentifier]] as its `transactionId` it will be assigned a new, valid one.
   * @param customerId The [[uk.co.randomcoding.partsdb.core.identifier.Identifier]] of the [[uk.co.randomcoding.partsdb.core.customer.Customer]] that this transaction is with.
   * @return a `Tuple2[Option[Document], Option[Transaction]]` that contains the successfully added document & transaction. If either fails to be added then `None` is returned
   */
  def addQuote(lineItems: List[LineItem], customerId: Identifier): (Option[Document], Option[Transaction]) = (None, None) /*= {
    val preQuote = assignId(Document(DefaultIdentifier, DocumentType.Quote, lineItems, DefaultIdentifier)).asInstanceOf[Document]
    val transaction: Transaction = assignId(Transaction(DefaultIdentifier, customerId, Some(Set(preQuote.documentId)))).asInstanceOf[Transaction]
    val quote = preQuote.copy(transactionId = transaction.transactionId)

    add(transaction) match {
      case true => add(quote) match {
        case true => {
          info("Added Quote: %s".format(quote))
          (Some(quote), Some(transaction))
        }
        case false => {
          error("Failed to add Quote: %s".format(quote))
          (None, Some(transaction))
        }
      }
      case false => {
        error("Failed to add Transaction: %s".format(transaction))
        (None, None)
      }
    }
  }*/
}
