/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import uk.co.randomcoding.partsdb.core.address.{ AddressParser, Address }
import uk.co.randomcoding.partsdb.core.contact.{ Phone, Mobile, Email, ContactDetails }
import uk.co.randomcoding.partsdb.core.terms.PaymentTerms
import uk.co.randomcoding.partsdb.core.util.CountryCodes.countryCodes
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet.{ ValidationItem, ErrorDisplay, DataValidation, StyleAttributes }
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
import uk.co.randomcoding.partsdb.core.customer.Customer

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddEditCustomer extends StatefulSnippet with ErrorDisplay with DataValidation with Logger {
  val terms = List(("30" -> "30"), ("45" -> "45"), ("60" -> "60"), ("90" -> "90"))

  val cameFrom = S.referer openOr "/app/show?entityType=Customer"
  //val cameFrom = S.referer openOr "/app/customers"
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
  private[this] def processSubmit() = {
    val billingAddress = addressFromInput(billingAddressText, billingAddressCountry)
    val contact = contactDetails(contactName, phoneNumber, mobileNumber, email, billingAddressCountry)

    val paymentTerms = asInt(paymentTermsText) match {
      case Full(terms) => PaymentTerms(terms)
      case _ => PaymentTerms(-1)
    }

    val validationChecks = Seq(
      ValidationItem(name, "errorMessages", "Customer Name must be entered"),
      ValidationItem(billingAddress, "errorMessages", "Billing Address is not valid"),
      ValidationItem(paymentTerms, "errorMessages", "Payment Terms are not valid"),
      ValidationItem(contact, "errorMessages", "Contact Details are not valid"))

    validate(validationChecks: _*) match {
      case Nil => {
        // Not sure if this is the best way to do this, should we do something to inform the user better if there is an error.
        /*if (addNewCustomer(name, billingAddress, paymentTerms, contact).isEmpty) {
          error("Failed to add new customer, [name: %s, address: %s, terms: %s, contact: %s".format(name, billingAddress, paymentTerms, contact))
        }*/

        S redirectTo "/app/show?entityType=Customer"
        //S redirectTo "/app/customers"
      }
      case errors => {
        errors foreach (error => displayError(error._1, error._2))
        Noop
      }
    }
  }

  private def addressFromInput(addressText: String, country: String): Option[Address] = {
    trace("Input address: %s, country: %s".format(addressText, country))
    val lines = scala.io.Source.fromString(addressText).getLines.toList
    val addressLines = lines.map(_.replaceAll(",", "").trim) ::: List(country)
    trace("Generated Address Lines: %s".format(addressLines))
    val shortName = "Short Name" // TODO: This should be setup correctly
    val address = addressLines mkString ","
    debug("Generating Address from: %s".format(address))

    (shortName, address) match {
      case AddressParser(addr) => {
        debug("Created Address: %s".format(addr))
        Some(addr)
      }
      case _ => {
        error("Null Address Created from %s".format(address))
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