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
import uk.co.randomcoding.partsdb.core.util.MongoHelpers._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddEditQuote extends StatefulSnippet with ErrorDisplay with DataValidation with LineItemSnippet with SubmitAndCancelSnippet with DocumentDataHolderTotalsDisplay with Logger {

  private[this] val originalQuote = S param "id" match {
    case Full(id) => Document.findById(id)
    case _ => None
  }

  override val cameFrom = () => S.referer openOr "/app"

  override val dataHolder = new QuoteDocumentDataHolder

  if (originalQuote.isDefined) dataHolder.populate(originalQuote.get)

  private[this] val customers = Customer orderDesc (_.customerName) fetch
  private[this] val customersSelect = (None, "Select Customer") :: (customers map ((c: Customer) => (Some(c), c.customerName.get)))

  private[this] var customerName = dataHolder.customer match {
    case Some(cust) => cust.customerName.get
    case _ => ""
  }

  def dispatch = {
    case "render" => render
  }

  def render = {
    "#formTitle" #> Text("Add Quote") &
      "#customerSelect" #> styledAjaxObjectSelect[Option[Customer]](customersSelect, dataHolder.customer, updateAjaxValue[Option[Customer]](dataHolder.customer = _)) &
      "#carriageEntry" #> styledAjaxText(dataHolder.carriageText, updateAjaxValue(dataHolder.carriage = _)) &
      renderAddEditLineItem() &
      renderSubmitAndCancel() &
      renderAllLineItems() &
      renderDocumentTotals()
  }

  private[this] val ensureAtLeastOneLineItemIsQuoted: () => Seq[String] = () => if (dataHolder.lineItems.isEmpty) Seq("Please include at least one item in the quote") else Nil

  override def processSubmit(): JsCmd = {
    performValidation(ensureAtLeastOneLineItemIsQuoted) match {
      case Nil => originalQuote match {
        case Some(quote) => updateQuote(quote, dataHolder.customer.get)
        case _ => addQuote(dataHolder.customer.get)
      }
      case errors => {
        displayErrors(errors: _*)
        Noop
      }
    }
  }

  override def validationItems() = Seq(ValidationItem(dataHolder.carriageValue, "Carriage"), ValidationItem(dataHolder.customer, "Quote Customer"))

  private[this] def updateQuote(quote: Document, cust: Customer): JsCmd = {
    val newQuote: Document = Quote.create(dataHolder.lineItems, dataHolder.carriageValue).documentAddress(Address.findById(dataHolder.customer.get.businessAddress.get).get)

    Document.update(originalQuote.get.id.get, newQuote) match {
      case Some(doc) if doc == quote => S.redirectTo("/app/")
      case Some(doc) => {
        error("Expected quote %s from update but got %s".format(quote, doc))
        displayError("Quote Update Failed. Please submit an Error report")
        Noop
      }
      case _ => {
        error("Failed to update quote")
        displayError("Quote Update Failed. Please submit an Error report")
        Noop
      }
    }
  }

  private[this] def addQuote(cust: Customer): JsCmd = {
    val quote: Document = Quote.create(dataHolder.lineItems, dataHolder.carriageValue).documentAddress(Address.findById(dataHolder.customer.get.businessAddress.get).get)

    Document.add(quote) match {
      case Some(q) => Transaction.add(cust, Seq(q)) match {
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
        error("Failed to add quote with items %s".format(dataHolder.lineItems.mkString("[", "\n", "]")))
        Noop
      }
    }
  }
}
