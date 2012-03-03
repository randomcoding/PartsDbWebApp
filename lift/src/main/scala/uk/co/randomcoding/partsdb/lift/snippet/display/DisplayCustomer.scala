/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet.display

import scala.xml.Text

import org.bson.types.ObjectId

import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.contact.ContactDetails
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet._

import net.liftweb.common.{ Logger, Full }
import net.liftweb.http.S
import net.liftweb.util.Helpers._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class DisplayCustomer extends ErrorDisplay with AddressSnippet with ContactDetailsSnippet with Logger {
  val terms = List(("30" -> "30"), ("45" -> "45"), ("60" -> "60"), ("90" -> "90"))

  val cameFrom = S.referer openOr "/app/show?entityType=Customer"

  val initialCustomer = S param "id" match {
    case Full(id) => Customer findById new ObjectId(id)
    case _ => None
  }

  var (name, paymentTermsText) = initialCustomer match {
    case Some(cust) => (cust.customerName.get, "%d".format(cust.terms.get))
    case _ => ("", "30")
  }

  override var (addressText, addressCountry) = initialCustomer match {
    case Some(cust) => Address findById cust.businessAddress.get match {
      case Some(addr) => (addr.addressText.get, addr.country.get)
      case _ => ("", "United Kingdom")
    }
    case _ => ("", "United Kingdom")
  }

  var (contactName, phoneNumber, mobileNumber, email) = initialCustomer match {
    case Some(cust) => cust.contactDetails.get match {
      case Nil => ("", "", "", "")
      case contacts => contacts map (ContactDetails findById _) filter (_.isDefined) map (_.get) find (_.isPrimary.get == true) match {
        case Some(c) => (c.contactName.get, c.phoneNumber.get, c.mobileNumber.get, c.emailAddress.get)
        case _ => ("", "", "", "")
      }
    }
    case _ => ("", "", "", "")
  }

  def render = {
    "#formTitle" #> Text("Display Customer") &
      "#nameEntry" #> styledText(name, name = _, readonly) &
      renderReadOnlyAddress() &
      "#paymentTermsEntry" #> styledText(paymentTermsText, paymentTermsText = _, List(readonly, ("style", "width: 2em"))) &
      renderReadOnlyContactDetails()
  }
}