/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import scala.xml.Text

import org.bson.types.ObjectId

import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.contact.ContactDetails
import uk.co.randomcoding.partsdb.core.part.PartCost
import uk.co.randomcoding.partsdb.core.supplier.Supplier
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet._

import net.liftweb.common.{ Logger, Full }
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.http.js.JsCmd.unitToJsCmd
import net.liftweb.http.js.JsCmd
import net.liftweb.http.{ StatefulSnippet, S }
import net.liftweb.util.Helpers._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddEditSupplier extends StatefulSnippet with AddressSnippet with ContactDetailsSnippet with PartCostSnippet with SubmitAndCancelSnippet with DataValidation with ErrorDisplay with Logger {

  override val cameFrom = S.referer openOr "app/show?entityType=Supplier"
  /*
   * Have we been called with an id= param that is the id of a Supplier?
   */
  val initialSupplier = S param ("id") match {
    case Full(id) => Supplier findById new ObjectId(id)
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
  override var (contactName, phoneNumber, mobileNumber, email) = initialSupplier match {
    case Some(s) => ContactDetails findById s.contactDetails.get match {
      case Some(c) => (c.contactName.get, c.phoneNumber.get, c.mobileNumber.get, c.emailAddress.get)
      case _ => ("", "", "", "")
    }
    case _ => ("", "", "", "")
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
      renderEditableAddress() &
      renderEditableContactDetails() &
      renderAddPartCost() &
      renderCurrentPartCosts() &
      renderSubmitAndCancel()
  }

  override def processSubmit(): JsCmd = {
    val address = addressFromInput(supplierName)
    val contacts = contactDetailsFromInput

    performValidation(address, contacts) match {
      case Nil => {
        val newContacts = updateContactDetails(contacts)
        val newAddress = updateAddress(address.get)

        initialSupplier match {
          case Some(s) => {
            modifySupplier(s, supplierName, newContacts.get, newAddress.get, currentPartCosts)
            S redirectTo cameFrom
          }
          case _ => addSupplier(supplierName, contacts, address.get, currentPartCosts) match {
            case Some(s) => S redirectTo cameFrom
            case _ => Noop
          }
        }
      }
      case errors => displayErrors(errors: _*)
    }
  }

  private[this] def performValidation(address: Option[Address], contacts: ContactDetails): Seq[String] = {
    val validationItems = Seq(
      ValidationItem(address, "Business Address"), //, "Address Entry was invalid"),
      ValidationItem(contacts, "Contact Details"), //, "Contact Details entry was invalid"),
      ValidationItem(supplierName, "Supplier Name")) //, "Supplier Name must be entered"))

    validate(validationItems: _*)
  }
}