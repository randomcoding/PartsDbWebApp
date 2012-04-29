/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet

import scala.io.Source
import scala.xml.Text
import uk.co.randomcoding.partsdb.core.address.{ AddressParser, Address }
import uk.co.randomcoding.partsdb.core.util.CountryCodes.countryCodes
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import net.liftweb.common.Logger
import net.liftweb.http.SHtml._
import net.liftweb.util.Helpers._
import scala.xml.NodeSeq
import uk.co.randomcoding.partsdb.core.customer.Customer

/**
 * A Snippet that renders and provides an [[uk.co.randomcoding.partsdb.core.address.Address]]
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait AddressSnippet extends Logger {

  var addressText: String
  var addressCountry: String
  var addressName = ""

  /**
   * Render address fields to allow the editing or creation of an address.
   *
   * The actual controls rendered are dependent on the provided parameters.
   *  - If the `addressLabel` is ''Business Address'' and `customer` is populated, then display '''Customer Name Business Address'''
   *  - If the `addressLabel` is ''Business Address'' and `customer` is empty, then the address name section is hidden completely. This is used to hide entry for auto populated values such as the New Customer address.
   *  - If the `addressLabel` is anything else and `customer` is empty then a default text display of '''New <address label>''' is used
   *  - Otherwise if the `customer` is populated then an editable text box is displayed to enable the user to enter their own name for the address.
   *
   * @param addressLabel The label to display for the address type.
   * @param customer An optional [[uk.co.randomcoding.partsdb.core.customer.Customer]] used to determine whether or not this address is for a new customer or not
   */
  def renderEditableAddress(addressLabel: String = "Business Address", customer: Option[Customer]) = {
    addressShortName(addressLabel, customer) &
      "#addressLabel" #> Text(addressLabel) &
      "#billingAddressEntry" #> styledTextArea(addressText, addressText = _) &
      "#billingAddressCountry" #> styledSelect(countryCodes, addressCountry, addressCountry = _)
  }

  /**
   * Render the address controls as read only.
   *
   * @param addressLabel The label to use for the address
   * @param customer An optional [[uk.co.randomcoding.partsdb.core.customer.Customer]]. This should not be `None`.
   */
  def renderReadOnlyAddress(addressLabel: String = "Business Address", customer: Option[Customer]) = {
    "#addressNameEntry" #> readOnlyAddressName(addressLabel, customer) &
      "#addressLabel" #> Text(addressLabel) &
      "#billingAddressEntry" #> styledTextArea(addressText, addressText = _, readonly) &
      "#billingAddressCountry" #> styledText(addressCountry, addressCountry = _, readonly)
  }

  private[this] def readOnlyAddressName(addressLabel: String, customer: Option[Customer]) = {
    addressName = customer match {
      case Some(cust) => (Address findById cust.businessAddress.get match {
        case Some(addr) => addr.shortName.get
        case _ => "%s for Unknown Address".format(addressLabel)
      })
      case _ => "%s for Unknown Customer".format(addressLabel)
    }

    Text(addressName)
  }

  private[this] def addressShortName(addressLabel: String = "Business Address", customer: Option[Customer]) = {
    (addressLabel, customer) match {
      case ("Business Address", Some(cust)) => setBusinessAddressNameAndRender(cust)
      case ("Business Address", None) => hiddenAddressName
      case (label, None) => "#addressNameEntry" #> Text("New %s".format(label))
      case (_, _) => "#addressNameEntry" #> styledAjaxText(addressName, addressName = _)
    }
  }

  private[this] def setBusinessAddressNameAndRender(cust: Customer) = {
    addressName = "%s Business Address".format(cust.customerName.get)
    "#addressNameEntry" #> Text(addressName)
  }
  private[this] def hiddenAddressName = "#addressNameSection" #> <div hidden="true">&nbsp;</div>

  def addressFromInput(name: String): Option[Address] = {
    trace("Input address: %s, country: %s".format(addressText, addressCountry))
    val lines = Source.fromString(addressText).getLines toList
    val addressLines = lines.map(_ replaceAll (",", "") trim)
    trace("Generated Address Lines: %s".format(addressLines))

    val address = addressLines mkString ("", ",", "")
    debug("Generating Address (%s) from: %s".format(name, address))

    (name, address, addressCountry) match {
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

  def updateAddress(address: Address): Option[Address] = {
    Address findMatching address match {
      case Some(a) => {
        Address.modify(a.id.get, address)
        Address findById a.id.get
      }
      case _ => Address add address
    }
  }
}