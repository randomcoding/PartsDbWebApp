/**
 *
 */
package uk.co.randomcoding.partsdb.db

import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.contact.ContactDetails
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.id.Identifier.longToIdentifier
import uk.co.randomcoding.partsdb.core.id.DefaultIdentifier
import uk.co.randomcoding.partsdb.core.terms.PaymentTerms
import uk.co.randomcoding.partsdb.db.mongo.{ MongoUpdateAccess, MongoIdentifierAccess, MongoConfig, MongoAllOrOneAccess }
import net.liftweb.common.Logger
import uk.co.randomcoding.partsdb.core.customer.DefaultCustomer

/**
 * Encapsulates all the Database access functionality in a single class
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
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
}