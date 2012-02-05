/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import uk.co.randomcoding.partsdb.lift.util.snippet.ErrorDisplay
import net.liftweb.common.{ Logger, Full }
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.http.js.JsCmd
import net.liftweb.http.{ StatefulSnippet, S }
import net.liftweb.util.Helpers._
import scala.xml.Text
import uk.co.randomcoding.partsdb.lift.util.snippet.AddressSnippet
import uk.co.randomcoding.partsdb.lift.util.snippet.ContactDetailsSnippet
import uk.co.randomcoding.partsdb.core.supplier.Supplier
import org.bson.types.ObjectId
import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.contact.ContactDetails
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet.ValidationItem
import uk.co.randomcoding.partsdb.lift.util.snippet.DataValidation
import uk.co.randomcoding.partsdb.core.part.PartCost

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddEditSupplier extends StatefulSnippet with AddressSnippet with ContactDetailsSnippet with DataValidation with ErrorDisplay with Logger {

  val cameFrom = S.referer openOr "app/show?entityType=Supplier"
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

  def dispatch = {
    case "render" => render
  }

  def render = {

    "#formTitle" #> Text("Add Supplier") &
      "#nameEntry" #> styledText(supplierName, supplierName = _) &
      renderAddress() &
      renderContactDetails() &
      "#submit" #> button("Submit", processSubmit)
  }

  private[this] def processSubmit(): JsCmd = {
    val address = addressFromInput(supplierName)
    val contacts = contactDetailsFromInput

    val validationItems = Seq(
      ValidationItem(address, "errorMessages", "Address Entry was invalid"),
      ValidationItem(contacts, "errorMessages", "Contact Details entry was invalid"))

    validate(validationItems: _*) match {
      case Nil => {
        initialSupplier match {
          case Some(s) => modifySupplier(s, supplierName, contacts, address.get)
          case _ => addSupplier(supplierName, contacts, address.get)
        }
      }
      case errors => displayError(errors: _*)
    }

    Noop
  }

  private[this] def addSupplier(name: String, contacts: ContactDetails, address: Address): JsCmd = {

    val contact = updateContactDetails(contacts)

    val addr = updateAddress(address)

    Supplier.add(name, contact.get, addr.get, Nil) match {
      case Some(s) => S redirectTo cameFrom
      case _ => Noop
    }
  }

  private[this] def modifySupplier(supplier: Supplier, newName: String, newContacts: ContactDetails, newAddress: Address): JsCmd = {
    val contacts = updateContactDetails(newContacts)

    val addr = updateAddress(newAddress)

    val suppliedParts = supplier.suppliedParts.get map (PartCost findById _) filter (_ isDefined) map (_ get)
    Supplier.modify(supplier.id.get, newName, contacts.get, addr.get, suppliedParts, supplier.notes.get)

    S redirectTo cameFrom
  }
}