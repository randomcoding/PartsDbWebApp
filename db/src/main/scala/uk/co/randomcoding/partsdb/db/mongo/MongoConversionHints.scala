/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import uk.co.randomcoding.partsdb.core.contact.ContactType
import uk.co.randomcoding.partsdb.core.contact.{ Phone, Mobile, Email }

import net.liftweb.json.JsonAST.{ JString, JObject, JField, JBool }
import net.liftweb.json.ShortTypeHints

/**
 * Provides hints and conversion rules for non case class objects.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object MongoConversionHints {

  /**
   * Hints for deserialising [[uk.co.randomcoding.partsdb.core.contact.ContactType]]s from Json
   */
  val contactTypeHints = new ShortTypeHints(classOf[ContactType] :: Nil) {

    override def deserialize: PartialFunction[(String, JObject), Any] = {
      case ("Email", JObject(JField("emailAddress", JString(emailAddress)) :: Nil)) => Email(emailAddress)
      case ("Phone", JObject(JField("phoneNumber", JString(phoneNumber)) :: JField("international", JBool(international)) :: Nil)) => Phone(phoneNumber, international)
      case ("Mobile", JObject(JField("mobileNumber", JString(mobileNumber)) :: JField("international", JBool(international)) :: Nil)) => Mobile(mobileNumber, international)
    }

    override def serialize: PartialFunction[Any, JObject] = {
      case email: Email => JObject(JField("emailAddress", JString(email.emailAddress)) :: Nil)
      case phone: Phone => JObject(JField("phoneNumber", JString(phone.phoneNumber)) :: JField("international", JBool(phone.international)) :: Nil)
      case mobile: Mobile => JObject(JField("mobileNumber", JString(mobile.mobileNumber)) :: JField("international", JBool(mobile.international)) :: Nil)
    }
  }
}