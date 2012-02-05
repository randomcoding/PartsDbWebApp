/**
 *
 */
package uk.co.randomcoding.partsdb.core.customer

import uk.co.randomcoding.partsdb.core.contact.ContactDetails
import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.id.{ Identifier, Identifiable, DefaultIdentifier }
import uk.co.randomcoding.partsdb.core.terms.PaymentTerms
import net.liftweb.mongodb.record.{ MongoRecord, MongoMetaRecord }
import net.liftweb.mongodb.record.field.{ ObjectIdPk, ObjectIdRefField, ObjectIdRefListField }
import net.liftweb.record.field.{ StringField, IntField }
import net.liftweb.common.Logger

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
  object contactDetails extends ObjectIdRefListField(this, ContactDetails)

  override def equals(that: Any): Boolean = {
    that.isInstanceOf[Customer] match {
      case false => false
      case true => {
        val other = that.asInstanceOf[Customer]

        customerName.get == other.customerName.get && businessAddress.get == other.businessAddress.get &&
          terms.get == other.terms.get && contactDetails.get.sortBy(_.toString) == other.contactDetails.get.sortBy(_.toString)
      }
    }
  }

  private val hashCodeFields = List(customerName, businessAddress, terms, contactDetails)

  override def hashCode: Int = getClass.hashCode + (hashCodeFields map (_.get.hashCode) sum)
}

object Customer extends Customer with MongoMetaRecord[Customer] with Logger {
  import org.bson.types.ObjectId
  import com.foursquare.rogue.Rogue._

  /**
   * Find a record that ''matches'' the provided one.
   *
   * A match is where:
   *  * There is a `Customer` with the same `ObjectId` as the provided one already present
   * '''or'''
   *  * There is a `Customer` with the same name
   *
   * If there are multiple matches (which there should not be) then this will return the ''first value'' which might not be consistently the same
   * as the query results are not sorted
   *
   * @return an optional value that will be populated with a matching record if there is one already present in the database
   */
  def findMatching(customer: Customer): Option[Customer] = {
    Customer.or(
      _.where(_.id eqs customer.id.get),
      _.where(_.customerName eqs customer.customerName.get)) get
  }

  /**
   * Add a new customer unless there is already a ''matching'' record. In which case the found entry is returned.
   *
   * This method assumes that the referenced [[uk.co.randomcoding.partsdb.core.address.Address]] and [[uk.co.randomcoding.partsdb.core.contactDetails.ContactDetails]]
   * already exist in the database.
   *
   * @return An `Option[Customer]`, populated if the addition was successful, or `None` if it failed.
   * If there are multiple customers with the same short name present, then this will return the first in the returned list
   * which is not guaranteed to be the same as the query is not ordered.
   */
  def add(customer: Customer): Option[Customer] = findMatching(customer) match {
    case Some(c) => Some(c)
    case _ => customer.save match {
      case c: Customer => Some(c)
      case _ => None
    }
  }

  /**
   * Add a new customer unless there is already a ''matching'' record. In which case the found entry is returned.
   *
   * @return An `Option[Customer]`, populated if the addition was successful, or `None` if it failed.
   * If there are multiple customers with the same short name present, then this will return the first in the returned list
   * which is not guaranteed to be the same as the query is not ordered.
   */
  def add(customerName: String, businessAddress: Address, termsDays: Int, contactDetails: ContactDetails): Option[Customer] = {
    val address = Address findMatching (businessAddress) match {
      case Some(addr) => Some(addr)
      case None => Address.add(businessAddress)
    }

    val contacts = ContactDetails findMatching contactDetails match {
      case Some(con) => Some(con)
      case None => ContactDetails.add(contactDetails)
    }

    (address, contacts) match {
      case (Some(addr), Some(cont)) => {
        add(Customer.createRecord.customerName(customerName).businessAddress(addr.id.get).terms(termsDays).contactDetails(cont.id.get :: Nil))
      }
      case (None, None) => {
        error("Failed to add Contact Details %s".format(contactDetails))
        error("Failed to add Address %s".format(businessAddress))
        None
      }
      case (None, _) => {
        error("Failed to add Address %s".format(businessAddress))
        None
      }
      case (_, None) => {
        error("Failed to add Contact Details %s".format(contactDetails))
        None
      }
    }
  }

  /**
   * Find a single customer by its Object Id
   *
   * @return An `Option[Customer]`, populated if the customer was found, or `None` if not.
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
   * If the `newContacts` are not already
   *
   * To keep a field with the same value, simply use the original value
   */
  def modify(oid: ObjectId, newName: String, newAddress: Address, newTerms: Int, newContacts: List[ContactDetails]) = {
    val address = Address findMatching (newAddress) match {
      case Some(addr) => Some(addr)
      case None => Address.add(newAddress)
    }

    require(address isDefined, "Failed to get valid address from: %s".format(newAddress))

    val contacts = newContacts map (newContact => ContactDetails findMatching newContact match {
      case Some(c) => Some(c)
      case _ => ContactDetails add (newContact)
    }) filter (_ isDefined) map (_ get)

    if (contacts.size != newContacts.size) error("Failed to add all new contacts the the database")

    Customer.where(_.id eqs oid).modify(_.customerName setTo newName) and
      (_.businessAddress setTo address.get.id.get) and
      (_.terms setTo newTerms) and
      (_.contactDetails setTo (contacts map (_.id.get))) updateMulti
  }
}
