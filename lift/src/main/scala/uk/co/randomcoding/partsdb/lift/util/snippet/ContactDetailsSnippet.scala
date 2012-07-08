/*
 * Copyright (C) 2012 RandomCoder <randomcoder@randomcoding.co.uk>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *    RandomCoder - initial API and implementation and/or initial documentation
 */

/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet

import scala.xml.Text

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
  var faxNumber: String

  val renderEditableContactDetails = () => {
    // TODO: Add a selector for isPrimary
    "#contactNameEntry" #> styledText(contactName, contactName = _) &
      "#phoneNumberEntry" #> styledText(phoneNumber, phoneNumber = _) &
      "#mobileNumberEntry" #> styledText(mobileNumber, mobileNumber = _) &
      "#faxNumberEntry" #> styledText(faxNumber, faxNumber = _) &
      "#emailEntry" #> styledText(email, email = _)
  }

  val renderReadOnlyContactDetails = () => {
    "#contactNameEntry" #> styledText(contactName, contactName = _, readonly) &
      "#phoneNumberEntry" #> styledText(phoneNumber, phoneNumber = _, readonly) &
      "#mobileNumberEntry" #> styledText(mobileNumber, mobileNumber = _, readonly) &
      "#faxNumberEntry" #> styledText(faxNumber, faxNumber = _, readonly) &
      "#emailEntry" #> styledText(email, email = _, readonly)
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
    val fax = faxNumber.trim

    ContactDetails.create(contactName, ph, mo, em, fax, true)
  }

  /*def updateContactDetails(contacts: ContactDetails): Option[ContactDetails] = ContactDetails findMatching contacts match {
    case Some(c) => {
      ContactDetails.modify(c.id.get, contacts)
      ContactDetails findById c.id.get
    }
    case _ => ContactDetails add contacts
  }*/
}
