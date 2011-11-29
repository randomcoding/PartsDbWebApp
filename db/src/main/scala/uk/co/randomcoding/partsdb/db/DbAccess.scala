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

  def addNewCustomer(contactName: String, billingAddress: Address, deliveryAddress: Address, terms: PaymentTerms, contact: ContactDetails) = {
    // check addresses is are in db or not and assign/get their Ids 
    val billingAddressId = DefaultIdentifier
    val deliveryAddressId = DefaultIdentifier
    val customer = Customer(-1L, contactName, billingAddressId, Set(deliveryAddressId), terms, contact)
    debug("Updating database with customer %s (billing: %s, delivery: %s)".format(customer, billingAddress, deliveryAddress))
  }
}