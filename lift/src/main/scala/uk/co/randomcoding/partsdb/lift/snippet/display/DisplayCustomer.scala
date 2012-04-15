/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet.display

import scala.xml.Text

import org.bson.types.ObjectId

import com.foursquare.rogue.Rogue._

import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.transaction.Transaction
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet._
import uk.co.randomcoding.partsdb.lift.util._

import net.liftweb.common.{Logger, Full}
import net.liftweb.http.{S, StatefulSnippet}
import net.liftweb.util.Helpers._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class DisplayCustomer extends StatefulSnippet with ErrorDisplay with AddressSnippet with ContactDetailsSnippet with TabDisplaySnippet with Logger {
  override val tabTitles = Seq(("quoteResults", "Quoted"), ("orderResults", "Ordered"), ("deliveryNoteResults", "Delivered"), ("invoiceResults", "Invoiced"), ("completedResults", "Completed"))

  private val cameFrom = S.referer openOr "/app/show?entityType=Customer"

  private val initialCustomer = S param "id" match {
    case Full(id) => Customer findById new ObjectId(id)
    case _ => None
  }

  private var (name, paymentTermsText) = initialCustomer match {
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

  override var (contactName, phoneNumber, mobileNumber, email, faxNumber) = initialCustomer match {
    case Some(cust) => cust.contactDetails.get match {
      case Nil => ("", "", "", "", "")
      case head :: tail => (head.contactName.get, head.phoneNumber.get, head.mobileNumber.get, head.emailAddress.get, head.faxNumber.get)
    }
    case _ => ("", "", "", "", "")
  }

  def customerId = initialCustomer match {
    case Some(cust) => cust.id.get.toString
    case _ => ""
  }

  private def transactions() = initialCustomer match {
    case Some(cust) => Transaction where (_.customer eqs cust.id.get) fetch
    case _ => List.empty
  }

  override def dispatch = {
    case "render" => render
  }

  def render = {
    val currentTransactions = transactions()
    "#formTitle" #> Text("Display Customer") &
        "#nameEntry" #> styledText(name, name = _, readonly) &
        renderReadOnlyAddress() &
        "#paymentTermsEntry" #> styledText(paymentTermsText, paymentTermsText = _, List(readonly, ("style", "width: 2em"))) &
        renderReadOnlyContactDetails() &
        "#recordPaymentButton" #> link("/app/recordPayment?customerId=%s".format(customerId), () => (), Text("Record Payment")) &
        "#documentTabs" #> generateTabs() &
        "#quotes" #> TransactionSummaryDisplay(currentTransactions filter (_.transactionState == "Quoted")) &
        "#orders" #> TransactionSummaryDisplay(currentTransactions filter (_.transactionState == "Ordered")) &
        "#deliveryNotes" #> TransactionSummaryDisplay(currentTransactions filter (_.transactionState == "Delivered")) &
        "#invoices" #> TransactionSummaryDisplay(currentTransactions filter (_.transactionState == "Invoiced")) &
        "#completed" #> TransactionSummaryDisplay(currentTransactions filter (_.transactionState == "Completed"))
  }
}
