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
class ContactDetails private () extends BsonRecord[ContactDetails] { // with ObjectIdPk[ContactDetails] {
  def meta = ContactDetails

  object contactName extends StringField(this, 50)
  object phoneNumber extends StringField(this, 50)
  object mobileNumber extends StringField(this, 50)
  object emailAddress extends StringField(this, 50)
  object faxNumber extends StringField(this, 50)
  object isPrimary extends BooleanField(this)

  override def equals(that: Any): Boolean = that match {
    case other: ContactDetails => {
      contactName.get == other.contactName.get &&
        phoneNumber.get == other.phoneNumber.get &&
        mobileNumber.get == other.mobileNumber.get &&
        emailAddress.get == other.emailAddress.get &&
        faxNumber.get == other.faxNumber.get
    }
    case _ => false
  }

  private val hashCodeFields = List(contactName, phoneNumber, mobileNumber, emailAddress, faxNumber)

  override def hashCode: Int = getClass.hashCode + (hashCodeFields map (_.get.hashCode) sum)

  def matches(other: ContactDetails): Boolean = {
    if (contactName.get == other.contactName.get) {
      phoneNumber.get == other.phoneNumber.get ||
        mobileNumber.get == other.mobileNumber.get ||
        emailAddress.get == other.emailAddress.get ||
        faxNumber.get == other.faxNumber.get
    }
    else false
  }
}

object ContactDetails extends ContactDetails with BsonMetaRecord[ContactDetails] {
  import org.bson.types.ObjectId
  import com.foursquare.rogue.Rogue._

  /**
   * Create a new `ContactDetails` record
   */
  def apply(contactName: String, phoneNumber: String, mobileNumber: String, emailAddress: String, faxNumber: String, isPrimary: Boolean): ContactDetails = {
    create(contactName, phoneNumber, mobileNumber, emailAddress, faxNumber, isPrimary)
  }

  /**
   * Create a new `ContactDetails` record
   */
  def create(contactName: String, phoneNumber: String, mobileNumber: String, emailAddress: String, faxNumber: String, isPrimary: Boolean): ContactDetails = {
    ContactDetails.createRecord.contactName(contactName).phoneNumber(phoneNumber).mobileNumber(mobileNumber).emailAddress(emailAddress).faxNumber(faxNumber).isPrimary(isPrimary)
  }

}