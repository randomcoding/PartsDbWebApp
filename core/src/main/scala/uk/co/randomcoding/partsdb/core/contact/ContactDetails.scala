/**
 *
 */
package uk.co.randomcoding.partsdb.core.contact

import net.liftweb.mongodb.record.field._
import net.liftweb.mongodb.record._
import net.liftweb.record.field._
import scala.math.Ordering.String
/**
 * Some contact details for a customer or supplier.
 *
 * Associates a phone number, mobile number and email to a name
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
class ContactDetails private () extends MongoRecord[ContactDetails] with ObjectIdPk[ContactDetails] {
  def meta = ContactDetails

  object contactName extends StringField(this, 50)
  object phoneNumber extends StringField(this, 50)
  object mobileNumber extends StringField(this, 50)
  object emailAddress extends StringField(this, 50)
  object isPrimary extends BooleanField(this)

  override def equals(that: Any): Boolean = {
    that.isInstanceOf[ContactDetails] match {
      case false => false
      case true => {
        val other = that.asInstanceOf[ContactDetails]
        contactName.get == other.contactName.get &&
          phoneNumber.get == other.phoneNumber.get &&
          mobileNumber.get == other.mobileNumber.get &&
          emailAddress.get == other.emailAddress.get

      }
    }
  }

  private val hashCodeFields = List(contactName, phoneNumber, mobileNumber, emailAddress)

  override def hashCode: Int = getClass.hashCode + (hashCodeFields map (_.get.hashCode) sum)
}

object ContactDetails extends ContactDetails with MongoMetaRecord[ContactDetails] {
  import org.bson.types.ObjectId
  import com.foursquare.rogue.Rogue._

  /**
   * Create a new `ContactDetails` record, but '''does not''' add it to the database
   */
  def create(contactName: String, phoneNumber: String, mobileNumber: String, emailAddress: String, isPrimary: Boolean): ContactDetails = {
    ContactDetails.createRecord.contactName(contactName).phoneNumber(phoneNumber).mobileNumber(mobileNumber).emailAddress(emailAddress).isPrimary(isPrimary)
  }

  /**
   * Find all records that have a `contactName` field with the given value
   */
  def findNamed(contactName: String): List[ContactDetails] = ContactDetails where (_.contactName eqs contactName) fetch

  /**
   * Find an optional record that is a `ContactDetails` and has the given `ObjectId`
   */
  def findById(oid: ObjectId): Option[ContactDetails] = ContactDetails where (_.id eqs oid) get

  /**
   * Find a record that ''matches'' the provided one.
   *
   * A match is where:
   *  * There is a `ContactDetails` with the same `ObjectId` as the provided one already present
   * '''or'''
   *  * There is a `Contact Details` with the same name and at least one of the `phone number`, `mobile number` or `email address` fields match
   *
   * @return an optional value that will be populated with a matching record if there is one already present in the database
   */
  def findMatching(contactDetails: ContactDetails): Option[ContactDetails] = findById(contactDetails.id.get) match {
    case Some(c) => Some(c)
    case _ => ContactDetails.where(_.contactName eqs contactDetails.contactName.get).or(
      _.where(_.phoneNumber eqs contactDetails.phoneNumber.get),
      _.where(_.mobileNumber eqs contactDetails.mobileNumber.get),
      _.where(_.emailAddress eqs contactDetails.emailAddress.get)).get

  }

  /**
   * Adds a `Contact Details` to the database unless there is a record that ''matches'' (by `findMatched`).
   *
   * If there is a matching record that is returned, even if this new record is different.
   *
   * To update a Contact Details object use the `modify` method
   *
   * If there is already a matching entry (as determined by the result of `findMatching(ContactDetails)` then this will be returned,
   * otherwise, the new record will be saved and if successful, will be returned as an `Option[ContactDetails]`.
   * If the save operation fails then `None` is returned
   */
  def add(contactDetails: ContactDetails): Option[ContactDetails] = findMatching(contactDetails) match {
    case Some(contacts) => Some(contacts)
    case _ => contactDetails.save match {
      case c: ContactDetails => Some(c)
      case _ => None
    }
  }

  /**
   * Adds a `Contact Details` created from the provided parameters to the database.
   *
   * If there is already a matching entry (as determined by the result of `findMatching(ContactDetails)` then this will be returned,
   * otherwise, the new record will be saved and if successful, will be returned as an `Option[ContactDetails]`.
   * If the save operation fails then `None` is returned
   */
  def add(contactName: String, phoneNumber: String, mobileNumber: String, emailAddress: String, isPrimary: Boolean): Option[ContactDetails] = {
    add(create(contactName, phoneNumber, mobileNumber, emailAddress, isPrimary))
  }

  /**
   * Remove `ContactDetails` with the given `ObjectId` if they exist
   */
  def remove(oid: ObjectId) = ContactDetails where (_.id eqs oid) bulkDelete_!!

  /**
   * Update the values of a record with the
   *
   * This will update '''ALL''' the fields so if you want to keep the same value for a field then pass it in as the new value
   */
  def modify(oid: ObjectId, newName: String, newPhoneNumber: String, newMobileNumber: String, newEMailAddress: String) = {
    ContactDetails.where(_.id eqs oid).modify(_.contactName setTo newName) and
      (_.phoneNumber setTo newPhoneNumber) and
      (_.mobileNumber setTo newMobileNumber) and
      (_.emailAddress setTo newEMailAddress) updateMulti
  }
}