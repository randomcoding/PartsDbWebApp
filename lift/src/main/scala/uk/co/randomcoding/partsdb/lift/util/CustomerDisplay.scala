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

/**
 * Helper functions for displaying customers in lift pages
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object CustomerDisplay extends Logger {
  /**
   * Generates html to display a customer.
   *
   * Currently displays the name, terms and contact details
   *
   * @param customer The [[import uk.co.randomcoding.partsdb.core.customer.Customer]] to display
   * @return A [[scala.xml.NodeSeq]] to display the customer details
   */
  def displayCustomer(customer: Customer): NodeSeq = {
    val custNameNodes = span(Text(customer.customerName), Noop)
    val termsNodes = span(Text("%d days".format(customer.terms.days)), Noop)
    val contactNodes = displayContacts(customer.contactDetails)
    val innerSpan = span(custNameNodes ++ termsNodes ++ contactNodes, Noop)

    val outerDiv = <div> { innerSpan } </div>
    outerDiv
  }

  private[this] def displayContacts(contacts: ContactDetails): NodeSeq = {
    val detailsNodes = (details: Seq[AnyRef]) => details map (detail => contactDetail(detail)) flatten
    implicit def optionListToList[T](opt: Option[List[T]]): List[T] = opt getOrElse List.empty[T]

    val phoneNodes = detailsNodes(contacts.phoneNumbers)
    val mobileNodes = detailsNodes(contacts.mobileNumbers)
    val emailNodes = detailsNodes(contacts.emailAddresses)

    emailNodes ++ mobileNodes ++ phoneNodes
  }

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