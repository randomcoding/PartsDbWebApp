/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util

import scala.io.Source
import scala.xml.{ Text, NodeSeq }

import org.bson.types.ObjectId

import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.contact.ContactDetails
import uk.co.randomcoding.partsdb.core.customer.Customer

import net.liftweb.common.Logger

/**
 * Helper functions for displaying customers in lift pages
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object CustomerDisplay extends EntityDisplay with Logger {
  type EntityType = Customer
  /**
   * The headings to use for the display of the customer data table
   */
  override val rowHeadings = List("Customer Name", "Address", "Contact", "Payment Terms")

  /**
   * Generates html to display a customer.
   *
   * Currently displays the name, terms and contact details
   *
   * @param customer The [[uk.co.randomcoding.partsdb.core.customer.Customer]] to display
   * @return A [[scala.xml.NodeSeq]] of `<td>` elements to display the customer details
   */
  override def displayEntity(customer: Customer): NodeSeq = {
    <td>{ customer.customerName }</td>
    <td>{ displayAddress(customer) }</td>
    <td>{ displayContacts(customer) }</td>
    <td>{ "%d days".format(customer.terms.get) }</td> ++
      editEntityCell(editEntityLink("Customer", customer.id.get))
  }

  private[this] def displayAddress(customer: Customer) = {
    debug("Displaying Details for Customer: %s".format(customer))
    Address findById customer.businessAddress.get match {
      case Some(addr) => {
        val addressLines = Source.fromString(addr.addressText.get).getLines()
        <span>{ addressLines map (line => <span>{ line }</span><br/>) }</span> ++
          <span>{ addr.country.get }</span>
      }
      case _ => Text("Unknown Address. Identifier: %s".format(customer.businessAddress.get))
    }
  }

  private[this] def displayContacts(customer: Customer): NodeSeq = {
    val contacts: List[ObjectId] = customer.contactDetails.get

    (for {
      contactId <- contacts
      val contact = ContactDetails.findById(contactId)
      if contact isDefined
    } yield {
      displayContact(contact.get)
    }) flatten

  }

  private[this] def displayContact(contactDetails: ContactDetails): NodeSeq = {
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