/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import scala.xml.Text

import org.bson.types.ObjectId

import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.contact.ContactDetails
import uk.co.randomcoding.partsdb.core.customer.Customer
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
class AddEditCustomer extends StatefulSnippet with ErrorDisplay with DataValidation with AddressSnippet with ContactDetailsSnippet with SubmitAndCancelSnippet with Logger {
  val terms = List(("30" -> "30"), ("45" -> "45"), ("60" -> "60"), ("90" -> "90"))

  override val cameFrom = S.referer openOr "/app/show?entityType=Customer"

  val initialCustomer = S param "id" match {
    case Full(id) => Customer findById new ObjectId(id)
    case _ => None
  }

  var (name, paymentTermsText) = initialCustomer match {
    case Some(cust) => (cust.customerName.get, "%d".format(cust.terms.get))
    case _ => ("", "30")
  }

  override var (addressText, addressCountry) = initialCustomer match {
    case Some(cust) => Address findById cust.businessAddress.get match {
      case Some(addr) => (addr.addressText.get, addr.country.get)
      case _ => ("", "United Kingdom")
    }
    case _ => ("", "United Kingdom")
  }

  override var (contactName, phoneNumber, mobileNumber, email, faxNumber) = initialCustomer match {
    case Some(cust) => cust.contactDetails.get match {
      case Nil => ("", "", "", "", "")
      case head :: tail => (head.contactName.get, head.phoneNumber.get, head.mobileNumber.get, head.emailAddress.get, head.faxNumber.get)
    }
    case _ => ("", "", "", "", "")
  }

  def dispatch = {
    case "render" => render
  }

  def render = {
    "#formTitle" #> Text("Add Customer") &
      "#nameEntry" #> styledText(name, name = _) &
      renderEditableAddress() &
      "#paymentTermsEntry" #> styledSelect(terms, paymentTermsText, paymentTermsText = _) &
      renderEditableContactDetails() &
      renderSubmitAndCancel()
  }

  /*
   * These are used to populate the validation
   */
  private[this] var billingAddress: Option[Address] = None
  private[this] var paymentTerms: Int = 0
  private[this] var contact: Option[ContactDetails] = None

  override def validationItems() = genValidationItems()

  private[this] def genValidationItems() = Seq(ValidationItem(billingAddress, "Business Address"),
    ValidationItem(paymentTerms, "Payment Terms"),
    ValidationItem(contact, "Contact Details"),
    ValidationItem(name, "Customer Name"))

  /**
   * Method called when the submit button is pressed.
   *
   * This extracts the details required to make the Customer object and if they validate, adds them to the database.
   *
   * On successful addition, this will (possibly display a dialogue and then) redirect to the main customers page
   */
  override def processSubmit(): JsCmd = {
    billingAddress = addressFromInput("%s Business Address".format(name))
    debug("Generated Address: %s".format(billingAddress))
    contact = Some(contactDetailsFromInput())

    paymentTerms = asInt(paymentTermsText) match {
      case Full(terms) => terms
      case _ => -1
    }

    trace("About to validate")

    performValidation() match {
      case Nil => {
        initialCustomer match {
          case None => addCustomer(billingAddress.get, paymentTerms, contact.get)
          case Some(c) => modifyCustomer(c, billingAddress.get, paymentTerms, contact.get)
        }
      }
      case errors => {
        errors foreach (error => displayError(error))
        Noop
      }
    }
  }

  private def addCustomer(billingAddress: Address, paymentTerms: Int, contact: ContactDetails): JsCmd = {
    Customer add (name, billingAddress, paymentTerms, contact) match {
      case Some(c) => S redirectTo cameFrom
      case _ => {
        error("Failed to add new customer, [name: %s, address: %s, terms: %s, contact: %s".format(name, billingAddress, paymentTerms, contact))
        displayError("Failed to add Customer")
        Noop
      }
    }
  }

  private def modifyCustomer(cust: Customer, billingAddress: Address, paymentTerms: Int, contact: ContactDetails): JsCmd = {
    val contacts = contact :: cust.contactDetails.get filterNot (_ matches contact)
    Customer.modify(cust.id.get, name, billingAddress, paymentTerms, contacts)
    S redirectTo cameFrom
  }
}