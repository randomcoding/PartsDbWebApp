/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import scala.xml.Text

import org.bson.types.ObjectId

import com.foursquare.rogue.Rogue._

import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.document.{ Order, LineItem, DocumentType, Document }
import uk.co.randomcoding.partsdb.core.part.Part
import uk.co.randomcoding.partsdb.core.transaction.Transaction
import uk.co.randomcoding.partsdb.lift.model.document.OrderDocumentDataHolder
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet.display.DocumentDataHolderTotalsDisplay
import uk.co.randomcoding.partsdb.lift.util.snippet._

import net.liftweb.common.Full
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.http.js.JsCmd
import net.liftweb.http.{ StatefulSnippet, S }
import net.liftweb.util.Helpers._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddEditOrder extends StatefulValidatingErrorDisplaySnippet with TransactionSnippet with DocumentDataHolderTotalsDisplay with SubmitAndCancelSnippet with AllLineItemsSnippet with AvailableLineItemsDisplay {

  override val cameFrom = S.referer openOr "/app/"

  override val dataHolder = new OrderDocumentDataHolder

  private var customerPoRef = ""
  private var confirmCloseQuote = false

  // Each transaction should only have a single Quote
  private[this] val quote = documentsOfType(DocumentType.Quote) headOption
  private[this] val orders = documentsOfType(DocumentType.Order)
  /*private[this] val (quote, orders) = transactionDocs match {
    case Nil => (None, Seq.empty)
    case docs => {
      val order = docs find (_.documentType.get == DocumentType.Quote)
      (order, docs filter (_.documentType.get == DocumentType.Order))
    }
  }*/

  private[this] val (carriage, lineItems, quoteId) = quote match {
    case Some(q) => {
      val orderedItems = orders flatMap (_.lineItems.get)
      dataHolder.carriage(q.carriage.get)
      (q.carriage.get, q.lineItems.get filterNot (orderedItems contains _) sortBy (_.lineNumber.get), q.documentNumber)
    }
    case _ => (0.0d, List.empty, "No Quote")
  }

  private[this] val validateQuoteCloseConfirmation = () => if (confirmCloseQuote) Nil else Seq("Please confirm it is ok to close the Quote before generating this Order")

  override def processSubmit(): JsCmd = {
    performValidation(validateQuoteCloseConfirmation) match {
      case Nil => {
        // create order
        val order = Document.add(Order(dataHolder.lineItems, dataHolder.carriageValue))
        order match {
          case Some(o) => {
            Transaction.addDocument(transaction.get.id.get, o.id.get)
            // 	close quote
            Document.close(quote.get.id.get)
            S redirectTo "/app/display/customer?id=%s".format(transaction.get.customer.get.toString)
          }
          case _ => {
            displayError("Failed to create Order. Please send an error report.")
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

  override def dispatch = {
    case "render" => render
  }

  def render = {
    "#formTitle" #> Text("Create Order") &
      "#transactionName" #> Text(transactionName) &
      "#customerName" #> Text(customerName) &
      renderDocumentTotals() &
      "#customerPoRefEntry" #> styledText(customerPoRef, customerPoRef = _) &
      renderAvailableLineItems(lineItems) &
      renderAllLineItems() &
      "#quoteId" #> Text(quoteId) &
      "#confirmCloseQuote" #> styledCheckbox(false, confirmCloseQuote = _) &
      renderSubmitAndCancel()
  }

  private[this] def validationItems: Seq[ValidationItem] = Seq(ValidationItem(customerPoRef, "Customer P/O Reference"),
    ValidationItem(dataHolder.lineItems, "Selected Line Items"))

  override def checkBoxSelected(selected: Boolean, line: LineItem) = {
    selected match {
      case true => {
        dataHolder.carriage(carriage)
        dataHolder.addLineItem(line)
      }
      case false => {
        dataHolder.carriage(0)
        dataHolder.removeLineItem(line)
      }
    }
    refreshLineItemDisplay()
  }
}