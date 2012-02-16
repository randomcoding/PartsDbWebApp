/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import scala.io.Source
import scala.xml.Text

import org.bson.types.ObjectId

import uk.co.randomcoding.partsdb.core.address.{ AddressParser, Address }
import uk.co.randomcoding.partsdb.core.contact.ContactDetails
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.util.CountryCodes.countryCodes
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet._

import net.liftweb.common.StringOrNodeSeq.strTo
import net.liftweb.common.{ Logger, Full }
import net.liftweb.http.SHtml._
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

  var (contactName, phoneNumber, mobileNumber, email) = initialCustomer match {
    case Some(cust) => cust.contactDetails.get match {
      case Nil => ("", "", "", "")
      case contacts => contacts map (ContactDetails findById _) filter (_.isDefined) map (_.get) find (_.isPrimary.get == true) match {
        case Some(c) => (c.contactName.get, c.phoneNumber.get, c.mobileNumber.get, c.emailAddress.get)
        case _ => ("", "", "", "")
      }
    }
    case _ => ("", "", "", "")
  }

  def dispatch = {
    case "render" => render
  }

  def render = {
    "#formTitle" #> Text("Add Customer") &
      "#nameEntry" #> styledText(name, name = _) &
      renderAddress() &
      "#paymentTermsEntry" #> styledSelect(terms, paymentTermsText, paymentTermsText = _) &
      renderContactDetails() &
      renderSubmitAndCancel()
  }

  /**
   * Method called when the submit button is pressed.
   *
   * This extracts the details required to make the Customer object and if they validate, adds them to the database.
   *
   * On successful addition, this will (possibly display a dialogue and then) redirect to the main customers page
   */
  override def processSubmit(): JsCmd = {
    val billingAddress = addressFromInput("%s Business Address".format(name))
    debug("Generated Address: %s".format(billingAddress))
    val contact = contactDetailsFromInput()

    val paymentTerms = asInt(paymentTermsText) match {
      case Full(terms) => terms
      case _ => -1
    }

    trace("About to validate")
    val validationChecks = Seq(ValidationItem(billingAddress, "errorMessages", "Business Address is not valid.\n Please ensure there is a Customer Name and that the country is selected."),
      ValidationItem(paymentTerms, "errorMessages", "Payment Terms are not valid"),
      ValidationItem(contact, "errorMessages", "Contact Details are not valid"),
      ValidationItem(name, "errorMessages", "Customer Name must be entered"))

    validate(validationChecks: _*) match {
      case Nil => {
        initialCustomer match {
          case None => addCustomer(billingAddress.get, paymentTerms, contact)
          case Some(c) => modifyCustomer(c, billingAddress.get, paymentTerms, contact)
        }
      }
      case errors => {
        errors foreach (error => displayError(error._1, error._2))
        Noop
      }
    }
  }

  private def addCustomer(billingAddress: Address, paymentTerms: Int, contact: ContactDetails): JsCmd = {
    Customer add (name, billingAddress, paymentTerms, contact) match {
      case Some(c) => S redirectTo cameFrom
      case _ => {
        error("Failed to add new customer, [name: %s, address: %s, terms: %s, contact: %s".format(name, billingAddress, paymentTerms, contact))
        displayError("errorMessages", "Failed to add Customer")
        Noop
      }
    }
  }

  private def modifyCustomer(cust: Customer, billingAddress: Address, paymentTerms: Int, contact: ContactDetails): JsCmd = {
    val contacts = contact :: (cust.contactDetails.get map (ContactDetails findById _) filter (_ isDefined) map (_ get))
    Customer.modify(cust.id.get, name, billingAddress, paymentTerms, contacts.distinct)
    S redirectTo cameFrom
  }
}