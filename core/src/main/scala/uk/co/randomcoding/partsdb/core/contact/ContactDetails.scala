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
        contactName == other.contactName &&
          phoneNumbers.get == other.phoneNumbers.get &&
          mobileNumbers.get == other.mobileNumbers.get &&
          emailAddresses.get == other.emailAddresses.get

      }
    }
  }

  def listCompare(list1: List[String], list2: List[String]): Boolean = list1.sorted(String) == list2.sorted(String)
}

object ContactDetails extends ContactDetails with MongoMetaRecord[ContactDetails] {

}