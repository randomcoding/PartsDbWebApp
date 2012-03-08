/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import scala.xml.Text

import com.foursquare.rogue.Rogue._

import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.document.Quote
import uk.co.randomcoding.partsdb.core.transaction.Transaction
import uk.co.randomcoding.partsdb.lift.model.document.QuoteHolder
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet._

import net.liftweb.common.Logger
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.http.js.JsCmd.unitToJsCmd
import net.liftweb.http.js.jquery.JqWiringSupport
import net.liftweb.http.js.JsCmd
import net.liftweb.http.{ WiringUI, StatefulSnippet, S }
import net.liftweb.util.Helpers._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddEditQuote extends StatefulSnippet with ErrorDisplay with DataValidation with LineItemSnippet with SubmitAndCancelSnippet with Logger {

  override val cameFrom = S.referer openOr "/app"

  var transactionName = ""
  var customerName = ""
  override val quoteHolder = new QuoteHolder

  val customers = Customer where (_.id exists true) orderDesc (_.customerName) fetch
  val customersSelect = (None, "Select Customer") :: (customers map ((c: Customer) => (Some(c), c.customerName.get)))
  var currentCustomer: Option[Customer] = None

  def dispatch = {
    case "render" => render
  }

  def render = {
    "#formTitle" #> Text("Add Quote") &
      "#transactionName" #> styledText(transactionName, transactionName = _) &
      "#customerSelect" #> styledObjectSelect[Option[Customer]](customersSelect, None, currentCustomer = _) &
      renderAddEditLineItem() &
      renderSubmitAndCancel() &
      renderAllLineItems() &
      "#subTotal" #> WiringUI.asText(quoteHolder.subTotal) &
      "#vatAmount" #> WiringUI.asText(quoteHolder.vatAmount) &
      "#totalCost" #> WiringUI.asText(quoteHolder.totalCost, JqWiringSupport.fade)
  }

  override def processSubmit(): JsCmd = currentCustomer match {
    case Some(cust) => addQuoteAndTransaction(cust)
    case None => displayError("Please select a Customer")
  }

  private[this] def addQuoteAndTransaction(cust: Customer): JsCmd = {
    validate(ValidationItem(transactionName, "Transaction Short Name" /*, "Please enter an Identifier for this Transaction"*/ )) match {
      case Nil => {
        Quote.add(quoteHolder.lineItems) match {
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
            error("Failed to add quote  with items %s".format(quoteHolder.lineItems.mkString("[", "\n", "]")))
            Noop
          }
        }
      }
      case errors => {
        displayErrors(errors: _*)
        Noop
      }
    }
  }

}
