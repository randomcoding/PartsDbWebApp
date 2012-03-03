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

/**
 * A Snippet that renders and provides an [[uk.co.randomcoding.partsdb.core.address.Address]]
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait AddressSnippet extends Logger {

  val readonly: ElemAttr = ("readonly", "readonly");
  var addressText: String
  var addressCountry: String

  val renderEditableAddress = () => {
    "#billingAddressEntry" #> styledTextArea(addressText, addressText = _) &
      "#billingAddressCountry" #> styledSelect(countryCodes, addressCountry, addressCountry = _)
  }

  val renderReadOnlyAddress = () => {
    "#billingAddressEntry" #> styledTextArea(addressText, addressText = _, readonly) &
      "#billingAddressCountry" #> Text(addressCountry);
  }

  def addressFromInput(name: String): Option[Address] = {
    trace("Input address: %s, country: %s".format(addressText, addressCountry))
    val lines = Source.fromString(addressText).getLines toList
    val addressLines = lines.map(_ replaceAll (",", "") trim)
    trace("Generated Address Lines: %s".format(addressLines))
    val shortName = "%s Business Address".format(name)
    val address = addressLines mkString ("", ",", "")
    debug("Generating Address (%s) from: %s".format(shortName, address))

    (shortName, address, addressCountry) match {
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