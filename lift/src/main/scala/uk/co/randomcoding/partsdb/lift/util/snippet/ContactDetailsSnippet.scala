/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet

import net.liftweb.common.{ Logger, Full }
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.http.js.JsCmd
import net.liftweb.http.{ StatefulSnippet, S }
import net.liftweb.util.Helpers._
import scala.xml.Text
import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.core.util.CountryCodes.countryCodes
import uk.co.randomcoding.partsdb.core.address.AddressParser
import scala.io.Source
import uk.co.randomcoding.partsdb.core.contact.ContactDetails
import org.bson.types.ObjectId

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait ContactDetailsSnippet extends Logger {

  var contactName: String
  var phoneNumber: String
  var mobileNumber: String
  var email: String

  val renderContactDetails = () => {
    // TODO: Add a selector for isPrimary
    "#contactNameEntry" #> styledText(contactName, contactName = _) &
      "#phoneNumberEntry" #> styledText(phoneNumber, phoneNumber = _) &
      "#mobileNumberEntry" #> styledText(mobileNumber, mobileNumber = _) &
      "#emailEntry" #> styledText(email, email = _)
  }

  /**
   * Create a [[uk.co.randomcoding.partsdb.core.contact.ContactDetails]] from the entered data
   *
   * Sets the `isPrimary` property to true
   */
  def contactDetailsFromInput(): ContactDetails = {
    val ph = phoneNumber.trim
    val mo = mobileNumber.trim
    val em = email.trim

    ContactDetails.create(contactName, ph, mo, em, true)
  }

  def updateContactDetails(contacts: ContactDetails): Option[ContactDetails] = ContactDetails findMatching contacts match {
    case Some(c) => {
      ContactDetails.modify(c.id.get, contacts)
      ContactDetails findById c.id.get
    }
    case _ => ContactDetails add contacts
  }
}