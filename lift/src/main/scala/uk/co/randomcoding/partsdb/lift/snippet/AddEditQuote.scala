/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import scala.xml.Text
import com.foursquare.rogue.Rogue._
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.document.Quote
import uk.co.randomcoding.partsdb.core.transaction.Transaction
import uk.co.randomcoding.partsdb.lift.model.document.DocumentDataHolder
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet._
import net.liftweb.common.Logger
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.http.js.JsCmd.unitToJsCmd
import net.liftweb.http.js.jquery.JqWiringSupport
import net.liftweb.http.js.JsCmd
import net.liftweb.http.{ WiringUI, StatefulSnippet, S }
import net.liftweb.util.Helpers._
import net.liftweb.common.Full
import uk.co.randomcoding.partsdb.lift.util.snippet.display.DocumentDataHolderTotalsDisplay
import uk.co.randomcoding.partsdb.lift.model.document.QuoteDocumentDataHolder
import uk.co.randomcoding.partsdb.core.document.Document
import uk.co.randomcoding.partsdb.core.address.Address

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddEditQuote extends StatefulSnippet with ErrorDisplay with DataValidation with LineItemSnippet with SubmitAndCancelSnippet with DocumentDataHolderTotalsDisplay with Logger {

  override val cameFrom = S.referer openOr "/app"

  var transactionName = ""
  var customerName = ""
  override val dataHolder = new QuoteDocumentDataHolder

  val customers = Customer orderDesc (_.customerName) fetch
  val customersSelect = (None, "Select Customer") :: (customers map ((c: Customer) => (Some(c), c.customerName.get)))
  var currentCustomer: Option[Customer] = None

  def dispatch = {
    case "render" => render
  }

  def render = {
    "#formTitle" #> Text("Add Quote") &
      "#transactionName" #> styledText(transactionName, transactionName = _) &
      "#customerSelect" #> styledObjectSelect[Option[Customer]](customersSelect, None, currentCustomer = _) &
      "#carriageEntry" #> styledAjaxText(dataHolder.carriageText, updateAjaxValue(dataHolder.carriage = _)) &
      renderAddEditLineItem() &
      renderSubmitAndCancel() &
      renderAllLineItems() &
      renderDocumentTotals()
  }

  override def processSubmit(): JsCmd = {
    val noCustomerError = "Please select a Customer"
    val noTransactionNameError = "Please Enter a Transaction Name"
    val duplicateTransactionNameError = "Please Enter a different Transaction Name, %s has already been used".format(transactionName)

    (currentCustomer, transactionName.trim isEmpty, isTransactionNameUnique) match {
      case (None, true, _) => displayErrors(noCustomerError, noTransactionNameError)
      case (None, _, false) => displayErrors(noCustomerError, duplicateTransactionNameError)
      case (Some(c), true, _) => displayErrors(noTransactionNameError)
      case (Some(c), _, false) => displayErrors(duplicateTransactionNameError)
      case (Some(cust), false, true) => addQuoteAndTransaction(cust)
    }
  }

  private[this] def isTransactionNameUnique: Boolean = (Transaction where (_.shortName eqs transactionName) get) isEmpty

  override def validationItems() = Seq(ValidationItem(transactionName, "Transaction Short Name"),
    ValidationItem(dataHolder.carriageValue, "Carriage"))

  private[this] def addQuoteAndTransaction(cust: Customer): JsCmd = performValidation() match {
    case Nil => addQuote(cust)
    case errors => {
      displayErrors(errors: _*)
      Noop
    }
  }

  private[this] def addQuote(cust: Customer) = {
    val quote: Document = Quote.create(dataHolder.lineItems, dataHolder.carriageValue).documentAddress(Address.findById(currentCustomer.get.businessAddress.get).get)

    Document.add(quote) match {
      case Some(q) => Transaction.add(transactionName, cust, Seq(q)) match {
        case Some(t) => {
          info("Successfully added quote %s to transaction %s".format(q, t))
          S.redirectTo("/app/")
        }
        case _ => {
          error("Added quote %s, but failed to add transaction".format(q))
          Noop
        }
      }
      case _ => {
        error("Failed to add quote  with items %s".format(dataHolder.lineItems.mkString("[", "\n", "]")))
        Noop
      }
    }
  }

}
