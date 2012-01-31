/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import scala.xml.Text
import uk.co.randomcoding.partsdb.core.address.{ AddressParser, Address }
import uk.co.randomcoding.partsdb.core.contact.{ Phone, Mobile, Email, ContactDetails }
import uk.co.randomcoding.partsdb.core.terms.PaymentTerms
import uk.co.randomcoding.partsdb.core.util.CountryCodes.countryCodes
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers.{ styledTextArea, styledText, styledSelect }
import uk.co.randomcoding.partsdb.lift.util.snippet.{ ValidationItem, ErrorDisplay, DataValidation }
import net.liftweb.common.StringOrNodeSeq.strTo
import net.liftweb.common.{ Logger, Full }
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.http.{ StatefulSnippet, S }
import net.liftweb.util.Helpers._
import uk.co.randomcoding.partsdb.core.customer.Customer
import net.liftweb.http.js.JsCmd

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddEditCustomer extends StatefulSnippet with ErrorDisplay with DataValidation with Logger {
  val terms = List(("30" -> "30"), ("45" -> "45"), ("60" -> "60"), ("90" -> "90"))

  val cameFrom = S.referer openOr "/app/customers"
  var name = ""
  var billingAddressText = ""
  var billingAddressCountry = "United Kingdom"
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
  private[this] def processSubmit(): JsCmd = {
    val billingAddress = addressFromInput(billingAddressText, billingAddressCountry)
    val contact = contactDetails(contactName, phoneNumber, mobileNumber, email, billingAddressCountry)

    val paymentTerms = asInt(paymentTermsText) match {
      case Full(terms) => PaymentTerms(terms)
      case _ => PaymentTerms(-1)
    }

    val validationChecks = Seq(ValidationItem(billingAddress, "businessAddressError", "Business Address is not valid.\n Please ensure there is a Customer Name and that the country is selected."),
      ValidationItem(paymentTerms, "paymentTermsError", "Payment Terms are not valid"),
      ValidationItem(contact, "contactDetailsError", "Contact Details are not valid"),
      ValidationItem(name, "customerNameError", "Customer Name must be entered"))

    validate(validationChecks: _*) match {
      case Nil => {
        addCustomer(billingAddress, paymentTerms, contact)
      }
      case errors => {
        errors foreach (error => displayError(error._1, error._2))
        Noop
      }
    }
  }

  private def addCustomer(billingAddress: Option[Address], paymentTerms: PaymentTerms, contact: ContactDetails) = {
    val address = Address.findByAddressText(billingAddressText) match {
      case Nil => billingAddress.get
      case results => results(0)
    }

    Customer.add(name, address, paymentTerms, contact) match {
      case Some(c) => S redirectTo "/app/customers"
      case _ => {
        error("Failed to add new customer, [name: %s, address: %s, terms: %s, contact: %s".format(name, billingAddress, paymentTerms, contact))
        displayError("addCustomerError", "Failed to add Customer")
        Noop
      }
    }
  }

  private def addressFromInput(addressText: String, country: String): Option[Address] = {
    trace("Input address: %s, country: %s".format(addressText, country))
    val lines = scala.io.Source.fromString(addressText).getLines.toList
    val addressLines = lines.map(_.replaceAll(",", "").trim) ::: List(country)
    trace("Generated Address Lines: %s".format(addressLines))
    val shortName = "%s Business Address".format(name)
    val address = addressLines mkString ","
    debug("Generating Address (%s) from: %s".format(shortName, address))

    (shortName, address) match {
      case AddressParser(addr) => {
        debug("Created Address: %s".format(addr))
        Some(addr)
      }
      case _ => {
        error("Null Adress Created from %s".format(address))
        None
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