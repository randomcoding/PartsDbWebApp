/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet

import uk.co.randomcoding.partsdb.core.contact.ContactDetails
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._

import net.liftweb.common.Logger
import net.liftweb.util.Helpers._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait ContactDetailsSnippet extends Logger {

  var contactName: String
  var phoneNumber: String
  var mobileNumber: String
  var email: String

  val renderEditableContactDetails = () => {
    // TODO: Add a selector for isPrimary
    "#contactNameEntry" #> styledText(contactName, contactName = _) &
      "#phoneNumberEntry" #> styledText(phoneNumber, phoneNumber = _) &
      "#mobileNumberEntry" #> styledText(mobileNumber, mobileNumber = _) &
      "#emailEntry" #> styledText(email, email = _)
  }

  val renderReadOnlyContactDetails = () => {

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