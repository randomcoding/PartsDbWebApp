/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import uk.co.randomcoding.partsdb.core.address.{ NullAddress, AddressParser, Address }
import uk.co.randomcoding.partsdb.core.contact.{ Phone, Mobile, Email, ContactDetails }
import uk.co.randomcoding.partsdb.core.terms.PaymentTerms
import uk.co.randomcoding.partsdb.core.util.CountryCodes.countryCodes
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet.{ ValidationItem, ErrorDisplay, DbAccessSnippet, DataValidation, StyleAttributes }
import uk.co.randomcoding.partsdb.lift.util.snippet.StyleAttributes._
import net.liftweb.common.StringOrNodeSeq.strTo
import net.liftweb.common.{ Logger, Full }
import net.liftweb.http.SHtml.{ select, button }
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.http.js.JsCmd
import net.liftweb.http.S
import net.liftweb.util.Helpers._
import scala.xml.Text
import net.liftweb.http.StatefulSnippet

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddCustomer extends StatefulSnippet with DbAccessSnippet with ErrorDisplay with DataValidation with Logger {
  val terms = List(("30" -> "30"), ("45" -> "45"), ("60" -> "60"), ("90" -> "90"))

  val cameFrom = S.referer openOr "/customers"
  var name = ""
  var billingAddressText = ""
  var billingAddressCountry = "United Kingdom"
  var deliveryAddressText = ""
  var deliveryAddressCountry = "United Kingdom"
  var paymentTermsText = "30"
  var contactName = ""
  var phoneNumber = ""
  var mobileNumber = ""
  var email = ""

  def dispatch = {
    case "render" => render
  }

  def render = {
    "#formTitle" #> Text("Add Customer") &
      "#nameEntry" #> styledText(name, name = _) &
      "#billingAddressEntry" #> styledTextArea(billingAddressText, billingAddressText = _) &
      "#billingAddressCountry" #> styledSelect(countryCodes, billingAddressCountry, billingAddressCountry = _) &
      "#deliveryAddressEntry" #> styledTextArea(deliveryAddressText, deliveryAddressText = _) &
      "#deliveryAddressCountry" #> styledSelect(countryCodes, deliveryAddressCountry, deliveryAddressCountry = _) &
      "#paymentTermsEntry" #> styledSelect(terms, paymentTermsText, paymentTermsText = _) &
      "#contactNameEntry" #> styledText(contactName, contactName = _) &
      "#phoneNumberEntry" #> styledText(phoneNumber, phoneNumber = _) &
      "#mobileNumberEntry" #> styledText(mobileNumber, mobileNumber = _) &
      "#emailEntry" #> styledText(email, email = _) &
      "#submit" #> button("Submit", processSubmit)
  }

  /**
   * Method called when the submit button is pressed.
   *
   * This extracts the details required to make the Customer object and if they validate, adds them to the database.
   *
   * On successful addition, this will (possibly display a dialogue and then) redirect to the main customers page
   */
  private[this] def processSubmit() = {
    val billingAddress = addressFromInput(billingAddressText, billingAddressCountry)
    val deliveryAddress = deliveryAddressText match {
      case "" => billingAddress
      case text => addressFromInput(text, deliveryAddressCountry)
    }

    val contact = contactDetails(contactName, phoneNumber, mobileNumber, email, billingAddressCountry)

    val paymentTerms = asInt(paymentTermsText) match {
      case Full(terms) => PaymentTerms(terms)
      case _ => PaymentTerms(-1)
    }

    val validationChecks = Seq(ValidationItem(billingAddress, "billingAddressError", "Billing Address is not valid"),
      ValidationItem(deliveryAddress, "deliveryAddressError", "Delivery Address is not valid"),
      ValidationItem(paymentTerms, "paymentTermsError", "Payment Terms are not valid"),
      ValidationItem(contact, "contactDetailsError", "Contact Details are not valid"),
      ValidationItem(name, "customerNameError", "Customer Name must be entered"))

    validate(validationChecks: _*) match {
      case Nil => {
        val newId = addNewCustomer(name, billingAddress, deliveryAddress, paymentTerms, contact).customerId
        S redirectTo "/customers?highlight=%d".format(newId.id)
      }
      case errors => {
        errors foreach (error => displayError(error._1, error._2))
        // TODO: Need to ensure that the entered details are still present
        Noop
      }
    }
  }

  private def addressFromInput(addressText: String, country: String): Address = {
    debug("Input address: %s, country: %s".format(addressText, country))
    val lines = scala.io.Source.fromString(addressText).getLines.toList
    val addressLines = lines.map(_.replaceAll(",", "").trim) ::: List(country)
    debug("Generated Address Lines: %s".format(addressLines))
    val address = addressLines mkString ","
    debug("Generating Address from: %s".format(address))
    address match {
      case AddressParser(addr) => {
        debug("Created Address: %s".format(addr))
        addr
      }
      case _ => {
        error("Null Adress Created from %s".format(address))
        NullAddress
      }
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