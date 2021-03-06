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
package uk.co.randomcoding.partsdb.lift.snippet

import scala.xml.Text

import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.contact.ContactDetails
import uk.co.randomcoding.partsdb.core.part.PartCost
import uk.co.randomcoding.partsdb.core.supplier.Supplier
import uk.co.randomcoding.partsdb.core.util.MongoHelpers._
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet._

import net.liftweb.common.{ Logger, Full }
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.http.js.JsCmd
import net.liftweb.http.{ StatefulSnippet, S }
import net.liftweb.util.Helpers._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddEditSupplier extends StatefulSnippet with AddressSnippet with ContactDetailsSnippet with PartCostSnippet with SubmitAndCancelSnippet with DataValidation with ErrorDisplay with Logger {

  override val cameFrom = () => "/app/show?entityType=Supplier"
  /*
   * Have we been called with an id= param that is the id of a Supplier?
   */
  val initialSupplier = S param ("id") match {
    case Full(id) => Supplier findById id
    case _ => None
  }

  /*
   * Set the address fields of the address snippet based on the initial supplier
   */
  override var (addressText, addressCountry) = initialSupplier match {
    case Some(s) => Address findById (s.businessAddress.get) match {
      case Some(addr) => (addr.addressText.get, addr.country.get)
      case _ => ("", "")
    }
    case _ => ("", "")
  }

  /*
   * Set the contact details fields of the contact details snippet based on the initial supplier
   */
  override var (contactName, phoneNumber, mobileNumber, email, faxNumber) = initialSupplier match {
    case Some(s) => s.contactDetails.get match {
      case c: ContactDetails => (c.contactName.get, c.phoneNumber.get, c.mobileNumber.get, c.emailAddress.get, c.faxNumber.get)
      case _ => ("", "", "", "", "")
    }
    case _ => ("", "", "", "", "")
  }

  /*
   * Set the supplier name based on the initial supplier
   */
  var supplierName = initialSupplier match {
    case Some(s) => s.supplierName.get
    case _ => ""
  }

  /*
   * Set the current part costs to display
   */
  override var currentPartCosts: List[PartCost] = initialSupplier match {
    case Some(s) => s.suppliedParts.get
    case _ => Nil
  }

  def dispatch = {
    case "render" => render
  }

  def render = {
    "#formTitle" #> Text("Add Supplier") &
      "#nameEntry" #> styledText(supplierName, supplierName = _) &
      renderEditableAddress("Business Address", None) &
      renderEditableContactDetails() &
      renderAddPartCost() &
      renderCurrentPartCosts() &
      renderSubmitAndCancel()
  }

  private[this] var address: Option[Address] = None
  private[this] var contacts: Option[ContactDetails] = None

  override def processSubmit(): JsCmd = {
    address = addressFromInput("%s Business Address".format(supplierName))
    contacts = Some(contactDetailsFromInput)

    performValidation() match {
      case Nil => {
        val newAddress = updateAddress(address.get)

        initialSupplier match {
          case Some(s) => {
            modifySupplier(s, supplierName, contacts.get, newAddress.get, currentPartCosts)
            S redirectTo cameFrom()
          }
          case _ => addSupplier(supplierName, contacts.get, newAddress.get, currentPartCosts) match {
            case Some(s) => S redirectTo cameFrom()
            case _ => Noop
          }
        }
      }
      case errors => displayErrors(errors: _*)
    }
  }

  /**
   * Provides validation for the Supplier details only
   */
  override def validationItems() = Seq(ValidationItem(address, "Business Address"),
    ValidationItem(contacts, "Contact Details"),
    ValidationItem(supplierName, "Supplier Name"))

}
