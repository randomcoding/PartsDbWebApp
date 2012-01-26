/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util

import uk.co.randomcoding.partsdb.core.contact.Mobile
import uk.co.randomcoding.partsdb.core.contact.Email
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmds.Noop
import uk.co.randomcoding.partsdb.core.contact.Phone
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.contact.ContactDetails
import scala.xml.NodeSeq
import scala.xml.Text
import net.liftweb.common.Logger
import scala.io.Source
import uk.co.randomcoding.partsdb.db.DbAccess
import uk.co.randomcoding.partsdb.lift.util.snippet.DbAccessSnippet
import uk.co.randomcoding.partsdb.core.address.Address

/**
 * Helper functions for displaying customers in lift pages
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object CustomerDisplay extends EntityDisplay with Logger with DbAccessSnippet {
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
    <td>{ "%d days".format(customer.terms.get.days) }</td> ++ <td></td>
    //editEntityCell(editEntityLink("Customer", customer.id.get))
  }

  private[this] def displayAddress(customer: Customer) = {
    debug("Displaying Details for Customer: %s".format(customer))
    //debug("Customer Billing Address Id: %s".format(customer.billingAddress))
    /*val addr = getOne[Address]("addressId", customer.billingAddress).getOrElse(NullAddress)
    addr match {
      case adr: Address => {
        val addressLines = Source.fromString(addr.addressText).getLines()
        <span>{ addressLines map (line => <span>{ line }</span><br/>) }</span>
      }
      case NullAddress _ => Text("Unknown Address. Identifier: %d".format(customer.businessAddress.get))
    }*/
  }

  private[this] def displayContacts(customer: Customer): NodeSeq = {
    val contacts = customer.contactDetails

    val detailsNodes = (details: Seq[AnyRef]) => details map (detail => contactDetail(detail)) flatten
    implicit def optionListToList[T](opt: Option[List[T]]): List[T] = opt getOrElse List.empty[T]

    val phoneNodes = detailsNodes(contacts.get.phoneNumbers)
    val mobileNodes = detailsNodes(contacts.get.mobileNumbers)
    val emailNodes = detailsNodes(contacts.get.emailAddresses)

    nameNode(contacts.get.contactName) ++ emailNodes ++ mobileNodes ++ phoneNodes
  }

  private[this] val nameNode = (name: String) => { <span>{ name }</span><br/> }

  private[this] def contactDetail(detail: AnyRef) = {
    debug("Generating contact detail for: %s".format(detail))
    val detailNode = detail match {
      case p: Phone => Text("Phone: %s".format(p.phoneNumber))
      case m: Mobile => Text("Mobile: %s".format(m.mobileNumber))
      case em: Email => Text("EMail: %s".format(em.emailAddress))
    }

    (<span>{ detailNode }</span><br/>).toSeq
  }
}