/**
 *
 */
package uk.co.randomcoding.partsdb.core.customer

import uk.co.randomcoding.partsdb.core.contact.ContactDetails
import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.id.{ Identifier, Identifiable, DefaultIdentifier }
import uk.co.randomcoding.partsdb.core.terms.PaymentTerms

import net.liftweb.mongodb.record.{ MongoRecord, MongoMetaRecord }
import net.liftweb.mongodb.record.field.{ ObjectIdPk, MongoCaseClassField, ObjectIdRefField }
import net.liftweb.record.field.{ StringField, IntField }

/**
 * Customer information, including the main business addresses, payment terms and contact details
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class Customer private () extends MongoRecord[Customer] with ObjectIdPk[Customer] {
  def meta = Customer

  object customerName extends StringField(this, 50)
  object businessAddress extends ObjectIdRefField(this, Address)
  object terms extends IntField(this)
  object contactDetails extends MongoCaseClassField[Customer, ContactDetails](this)

  override def equals(that: Any): Boolean = {
    that.isInstanceOf[Customer] match {
      case false => false
      case true => {
        val other = that.asInstanceOf[Customer]

        customerName.get == other.customerName.get && businessAddress.get == other.businessAddress.get &&
          terms.get == other.terms.get && contactDetails.get == other.contactDetails.get
      }
    }
  }

  private val hashCodeFields = List(customerName, businessAddress, terms, contactDetails)

  override def hashCode: Int = getClass.hashCode + (hashCodeFields map (_.get.hashCode) sum)
}

object Customer extends Customer with MongoMetaRecord[Customer] {
  import org.bson.types.ObjectId
  import com.foursquare.rogue.Rogue._

  /**
   * Add a new customer unless there is already one with the same `customerName`
   *
   * @return An `Option[Customer]`, populated if the addition was successful, or `None` if it failed
   */
  def add(customerName: String, businessAddress: Address, termsDays: Int, contactDetails: ContactDetails) = findNamed(customerName) match {
    case Nil => {
      Customer.createRecord.customerName(customerName).businessAddress(businessAddress.id.get).terms(termsDays).contactDetails(contactDetails).save match {
        case cust: Customer => Some(cust)
        case _ => None
      }
    }
    case _ => None
  }

  /**
   * Find a single customer by its Object Id
   *
   * @return An `Oprion[Customer]`, populated if the customer was found, or `None` if not.
   */
  def findById(oid: ObjectId) = Customer where (_.id eqs oid) get

  /**
   * Find all `Customer`s with the given `customerName`
   *
   * As we try and ensure that all customers' `customerName`s are unique this '''should''' only return a single record.
   *
   * @return A list of `Customer`s that all have the same `customerName`
   */
  def findNamed(customerName: String) = Customer where (_.customerName eqs customerName) fetch

  /**
   * Remove a `Customer` with a given `ObjectId`.
   *
   * This '''should''' only affect a single record
   *
   * @return A list of `Boolean` values. If all removes succeeded this will only contain `true`
   */
  def remove(oid: ObjectId) = (Customer where (_.id eqs oid) fetch) map (_ delete_!) distinct

  /**
   * Update the values of '''''all''''' the fields of a `Customer`.
   *
   * To keep a field with the same value, simply use the original value
   */
  def modify(oid: ObjectId, newName: String, newAddress: Address, newTerms: Int, newContacts: ContactDetails) = {
    Customer.where(_.id eqs oid).modify(_.customerName setTo newName) and (_.businessAddress setTo newAddress.id.get) and (_.terms setTo newTerms) and (_.contactDetails setTo newContacts) updateMulti
  }
}
