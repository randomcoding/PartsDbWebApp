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
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
class ContactDetails private () extends MongoRecord[ContactDetails] with ObjectIdPk[ContactDetails] {
  def meta = ContactDetails

  object contactName extends StringField(this, 50)
  object phoneNumbers extends StringField(this, 50)
  object mobileNumbers extends StringField(this, 50)
  object emailAddresses extends StringField(this, 50)

  override def equals(that: Any): Boolean = {
    that.isInstanceOf[ContactDetails] match {
      case false => false
      case true => {
        val other = that.asInstanceOf[ContactDetails]
        contactName.get == other.contactName.get &&
          phoneNumbers.get == other.phoneNumbers.get &&
          mobileNumbers.get == other.mobileNumbers.get &&
          emailAddresses.get == other.emailAddresses.get

      }
    }
  }

  private val hashCodeFields = List(contactName, phoneNumbers, mobileNumbers, emailAddresses)

  override def hashCode: Int = getClass.hashCode + (hashCodeFields map (_.get.hashCode) sum)
}

object ContactDetails extends ContactDetails with MongoMetaRecord[ContactDetails] {
  import org.bson.types.ObjectId
  import com.foursquare.rogue.Rogue._

  def findNamed(contactName: String) = ContactDetails where (_.contactName eqs contactName) fetch

  def findById(oid: ObjectId) = ContactDetails where (_.id eqs oid) get

  def add(contactName: String, phoneNumbers: String, mobileNumbers: String, emailAddresses: String) = findNamed(contactName) match {
    case Nil => {
      ContactDetails.createRecord.contactName(contactName).phoneNumbers(phoneNumbers).mobileNumbers(mobileNumbers).emailAddresses(emailAddresses).save match {
        case cont: ContactDetails => Some(cont)
        case _ => None
      }
    }
    case _ => None
  }

  def remove(oid: ObjectId) = findById(oid) match {
    case Some(c) => List(c delete_!)
    case _ => Nil
  }

  def modify(oid: ObjectId, newName: String, newPhoneNumbers: String, newMobileNumbers: String, newEMailAddresses: String) = {
    findById(oid) match {
      case Some(c) => {
        ContactDetails.where(_.id eqs oid).modify(_.contactName setTo newName) and
          (_.phoneNumbers setTo newPhoneNumbers) and
          (_.mobileNumbers setTo newMobileNumbers) and
          (_.emailAddresses setTo newEMailAddresses) updateMulti
      }
      case _ => // Do nothing
    }
  }
}