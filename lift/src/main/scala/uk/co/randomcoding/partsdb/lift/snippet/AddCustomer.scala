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

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddCustomer extends DbAccessSnippet with ErrorDisplay with DataValidation with Logger {
  val terms = List(("30" -> "30"), ("45" -> "45"), ("60" -> "60"), ("90" -> "90"))

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
      val deliveryAddress = deliveryAddressText match {
        case "" => billingAddress
        case text => addressFromInput(text, deliveryAddressCountry)
      }

      val contact = contactDetails(contactName, phoneNumber, mobileNumber, email, billingAddressCountry)

      val paymentTerms = asInt(paymentTermsText) match {
        case Full(terms) => PaymentTerms(terms)
        case _ => PaymentTerms(-1)
      }

      validate(ValidationItem(billingAddress, "billingAddressError", "Billing Address is not valid"),
        ValidationItem(deliveryAddress, "deliveryAddressError", "Delivery Address is not valid"),
        ValidationItem(paymentTerms, "paymentTermsError", "Payment Terms are not valid")) match {
          case Nil => {
            val newId = addNewCustomer(contactName, billingAddress, deliveryAddress, paymentTerms, contact).customerId
            S.redirectTo("/customers?highlight=%d".format(newId.id))
          }
          case errors => {
            errors foreach (error => displayError(error._1, error._2))
            // TODO: Need to ensure that the entered details are still present
            Noop
          }
        }
    }

    "#nameEntry" #> styledText("", name = _) &
      "#billingAddressEntry" #> styledTextArea("", billingAddressText = _) &
      "#billingAddressCountry" #> styledSelect(countryCodes, "United Kingdom", billingAddressCountry = _) &
      "#deliveryAddressEntry" #> styledTextArea("", deliveryAddressText = _) &
      "#deliveryAddressCountry" #> styledSelect(countryCodes, "United Kingdom", deliveryAddressCountry = _) &
      "#contactNameEntry" #> styledText("", contactName = _) &
      "#paymentTermsEntry" #> styledSelect(terms, "30", paymentTermsText = _) &
      "#phoneNumberEntry" #> styledText("", phoneNumber = _) &
      "#mobileNumberEntry" #> styledText("", mobileNumber = _) &
      "#emailEntry" #> styledText("", email = _) &
      "#submit" #> button("Submit", processSubmit)

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