/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import net.liftweb.util.Helpers._
import net.liftweb.http.SHtml._
import uk.co.randomcoding.partsdb.core.address.{ NullAddress, Address }
import uk.co.randomcoding.partsdb.core.contact.{ ContactDetails, NullContactDetails }
import uk.co.randomcoding.partsdb.core.id.Identifier

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddCustomer {
  def render = {
    var name = ""
    var billingAddress: Address = NullAddress
    var deliveryAddress = NullAddress
    var paymentTerms = 30
    var contactDetails: ContactDetails = NullContactDetails

    def processSubmit() = {

    }

    "#nameEntry" #> text("", name = _) &
      "#billingAddressEntry" #> textarea("", addressText => billingAddress = parseAddressText(addressText))

  }

  // This needs to go into an Object in the core package
  private def parseAddressText(addressText: String): Address = {
    val addressSource = scala.io.Source.fromString(addressText)
    val shortName = addressSource.getLine(0)
    val country = addressSource.getLine(addressSource.length - 1)
    Address(Identifier(0), shortName, addressText, country)
  }
}