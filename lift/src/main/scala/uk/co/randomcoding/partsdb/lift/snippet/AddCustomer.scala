/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import scala.io.Source.fromString
import uk.co.randomcoding.partsdb.core.address.{ NullAddress, Address, AddressParser }
import uk.co.randomcoding.partsdb.core.contact.{ NullContactDetails, ContactDetails }
import uk.co.randomcoding.partsdb.core.id.{ Identifier, DefaultIdentifier }
import uk.co.randomcoding.partsdb.core.id.Identifier._
import uk.co.randomcoding.partsdb.core.util.CountryCodes.countryCodes
import uk.co.randomcoding.partsdb.core.contact._
import uk.co.randomcoding.partsdb.core.terms.PaymentTerms
import uk.co.randomcoding.partsdb.core.customer.Customer
import net.liftweb.common.Full
import net.liftweb.http.S
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.http.SHtml._
import net.liftweb.util.Helpers._
import net.liftweb.http.js.JsCmd
import net.liftweb.common.Logger
import uk.co.randomcoding.partsdb.lift.util.snippet.ErrorDisplay
import uk.co.randomcoding.partsdb.lift.util.snippet.DbAccessSnippet
import uk.co.randomcoding.partsdb.lift.util.snippet.DataValidation
import uk.co.randomcoding.partsdb.lift.util.snippet.ValidationItem

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddCustomer extends DbAccessSnippet with ErrorDisplay with DataValidation with Logger {
  def render = {
    var name = ""
    var billingAddressText = ""
    var billingAddressCountry = ""
    var deliveryAddressText = ""
    var deliveryAddressCountry = ""
    var paymentTermsText = ""
    var contactName = ""
    var phoneNumber = ""
    var mobileNumber = ""
    var email = ""

    /**
     * Method called when the submit button is pressed.
     *
     * This extracts the details required to make the Customer object and if they validate, adds them to the database.
     *
     * On successful addition, this will (possibly display a dialogue and then) redirect to the main customers page
     */
    def processSubmit(): JsCmd = {
      val billingAddress = addressFromInput(billingAddressText, billingAddressCountry)
      val deliveryAddress = addressFromInput(deliveryAddressText, deliveryAddressCountry)
      val contact = contactDetails(contactName, phoneNumber, mobileNumber, email, billingAddressCountry)

      val paymentTerms = asInt(paymentTermsText) match {
        case Full(terms) => PaymentTerms(terms)
        case _ => PaymentTerms(-1)
      }

      validate(ValidationItem(billingAddress, "billingAddressError", "Billing Address is not valid"),
        ValidationItem(deliveryAddress, "deliveryAddressError", "Delivery Address is not valid"),
        ValidationItem(paymentTerms, "paymentTermsError", "Payment Terms are not valid")) match {
          case Nil => {
            addNewCustomer(contactName, billingAddress, deliveryAddress, paymentTerms, contact)
            S.redirectTo("/customers")
          }
          case errors => {
            errors foreach (error => displayError(error._1, error._2))
            // TODO: Need to ensure that the entered details are still present
            Noop
          }
        }
    }

    "#nameEntry" #> text("", name = _) &
      "#billingAddressEntry" #> textarea("", billingAddressText = _) &
      "#billingAddressCountry" #> select(countryCodes, Full("United Kingdom"), billingAddressCountry = _) &
      "#deliveryAddressEntry" #> textarea("", deliveryAddressText = _) &
      "#deliveryAddressCountry" #> select(countryCodes, Full("United Kingdom"), deliveryAddressCountry = _) &
      "#contactNameEntry" #> text("", contactName = _) &
      "#paymentTermsEntry" #> text("", paymentTermsText = _) &
      "#phoneNumberEntry" #> text("", phoneNumber = _) &
      "#mobileNumberEntry" #> text("", mobileNumber = _) &
      "#emailEntry" #> text("", email = _) &
      "#submit" #> button("Submit", processSubmit)

  }

  private def addressFromInput(addressText: String, country: String): Address = {
    addressText match {
      case AddressParser(addr) => addr
      case _ => NullAddress
    }
  }

  private def contactDetails(name: String, phone: String, mobile: String, email: String, countryCode: String): ContactDetails = {
    val isInternational = countryCode == "UK"
    val ph = if (phone.trim.isEmpty) None else Some(List(Phone(phone, isInternational)))
    val mo = if (mobile.trim.isEmpty) None else Some(List(Mobile(mobile, isInternational)))
    val em = if (email.trim.isEmpty) None else Some(List(Email(email)))

    ContactDetails(name, ph, mo, em)
  }
}