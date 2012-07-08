/*
 * Copyright (C) 2012 RandomCoder <randomcoder@randomcoding.co.uk>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *    RandomCoder - initial API and implementation and/or initial documentation
 */

/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet

import scala.io.Source
import scala.xml.Text

import uk.co.randomcoding.partsdb.core.address.{ AddressParser, Address }
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.util.CountryCodes.{ keyForCountry, countryCodes }
import uk.co.randomcoding.partsdb.core.util.CountryCode
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._

import net.liftweb.common.Logger
import net.liftweb.util.Helpers._

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
      "#billingAddressCountry" #> styledSelect(countryCodes, keyForCountry(addressCountry), addressCountry = _)
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

  /**
   * Try and generate an address from the input provided in the address entry fields.
   *
   * Uses the [[uk.co.randomcoding.partsdb.core.address.AddressParser]] with an input
   * tuple `(name, addressText, addressCountry)`
   *
   * @return A populated `Option` with the parsed address if one could be generated, `None` otherwise.
   */
  def addressFromInput(name: String): Option[Address] = {
    val addressLines = Source.fromString(addressText).getLines.toList

    debug("Attempting to generate address (%s) from: %s, %s".format(name, addressLines mkString ("", ",", ""), addressCountry))

    (name, addressLines, addressCountry) match {
      case AddressParser(addr) => {
        debug("Created Address: %s".format(addr))
        Some(addr)
      }
      case _ => {
        error("Null Adress Created from %s".format(addressLines mkString ("", ",", "")))
        None
      }
    }
  }

  /**
   * Update an address record in the database.
   *
   * If the address exists (a match is found) then the record is updated
   * otherwise a new record is added.
   */
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
