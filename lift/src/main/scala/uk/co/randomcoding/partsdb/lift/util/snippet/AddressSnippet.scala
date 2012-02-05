/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet

import net.liftweb.common.{ Logger, Full }
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.http.js.JsCmd
import net.liftweb.http.{ StatefulSnippet, S }
import net.liftweb.util.Helpers._
import scala.xml.Text
import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.core.util.CountryCodes.countryCodes
import uk.co.randomcoding.partsdb.core.address.AddressParser
import scala.io.Source

/**
 * A Snippet that renders and provides an [[uk.co.randomcoding.partsdb.core.address.Address]]
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait AddressSnippet extends Logger {

  var addressText: String
  var addressCountry: String

  val renderAddress = () => {
    "#billingAddressEntry" #> styledTextArea(addressText, addressText = _) &
      "#billingAddressCountry" #> styledSelect(countryCodes, addressCountry, addressCountry = _)
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