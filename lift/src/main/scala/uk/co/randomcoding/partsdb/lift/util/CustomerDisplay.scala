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
import SnippetDisplayHelpers._

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
      case Some(addr) => displayAddressCell(addr)
      case _ => Text("Unknown Address. Identifier: %s".format(customer.businessAddress.get))
    }
  }

  private[this] def displayContacts(customer: Customer): NodeSeq = {
    (for {
      contactId <- customer.contactDetails.get
      val contact = ContactDetails.findById(contactId)
      if contact isDefined
    } yield {
      displayContactCell(contact.get)
    }) flatten
  }
}