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
import uk.co.randomcoding.partsdb.core.address.{ Address, NullAddress }

/**
 * Helper functions for displaying customers in lift pages
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object CustomerDisplay extends Logger with DbAccessSnippet {

  /**
   * The headings to use for the display of the customer data table
   */
  val customerHeadings = List("Customer Name", "Address", "Contact", "Payment Terms")

  /**
   * Generates html to display a customer.
   *
   * Currently displays the name, terms and contact details
   *
   * @param customer The [[uk.co.randomcoding.partsdb.core.customer.Customer]] to display
   * @return A [[scala.xml.NodeSeq]] to display the customer details
   */
  def displayCustomer(customer: Customer): NodeSeq = {
    <tr valign="top">
      <td>{ customer.customerName }</td>
      <td>{ displayAddress(customer) }</td>
      <td>{ displayContacts(customer) }</td>
      <td>{ "%d days".format(customer.terms.days) }</td>
    </tr>
  }

  private[this] def displayAddress(customer: Customer) = {
    debug("Displaying Details for Customer: %s".format(customer))
    debug("Customer Billing Address Id: %s".format(customer.billingAddress))
    val addr = getOne[Address]("addressId", customer.billingAddress).getOrElse(NullAddress)
    addr match {
      case NullAddress => Text("Unknown Address. Identifier: %d".format(customer.billingAddress.id))
      case adr: Address => {
        val addressLines = Source.fromString(addr.addressText).getLines()
        <span>
          {
            addressLines map (line => <span>{ line }</span><br/>)
          }
        </span>
      }
    }
  }

  private[this] def displayContacts(customer: Customer): NodeSeq = {
    val contacts = customer.contactDetails

    val detailsNodes = (details: Seq[AnyRef]) => details map (detail => contactDetail(detail)) flatten
    implicit def optionListToList[T](opt: Option[List[T]]): List[T] = opt getOrElse List.empty[T]

    val phoneNodes = detailsNodes(contacts.phoneNumbers)
    val mobileNodes = detailsNodes(contacts.mobileNumbers)
    val emailNodes = detailsNodes(contacts.emailAddresses)

    nameNode(contacts.contactName) ++ emailNodes ++ mobileNodes ++ phoneNodes
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