/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util

import scala.io.Source
import scala.xml.NodeSeq

import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.contact.ContactDetails

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object SnippetDisplayHelpers {

  def displayAddressCell(address: Address): NodeSeq = {
    val addressLines = Source.fromString(address.addressText.get).getLines()
    <span>{ addressLines map (line => <span>{ line }</span><br/>) }</span> ++
      <span>{ address.country.get }</span>
  }

  def displayContactCell(contactDetails: ContactDetails): NodeSeq = {
    <span>{ contactDetails.contactName.get }</span><br/>
    ++ numbersDetails (contactDetails)
  }

  private[this] def numbersDetails(contactDetails: ContactDetails): NodeSeq = {
    val details = (detailString: String, heading: String) =>
      detailString.trim match {
        case "" => <span>&nbsp;</span><br/>
        case other => <span>{ "%s: %s".format(heading, detailString) }</span><br/>
      }

    details(contactDetails.phoneNumber.get, "Phone") ++
      details(contactDetails.mobileNumber.get, "Mobile") ++
      details(contactDetails.emailAddress.get, "EMail")
  }
}