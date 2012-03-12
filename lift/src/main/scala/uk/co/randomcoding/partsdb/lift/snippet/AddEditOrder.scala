/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import scala.xml.Text
import org.bson.types.ObjectId
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.document.{ LineItem, DocumentType, Document }
import uk.co.randomcoding.partsdb.core.part.Part
import uk.co.randomcoding.partsdb.core.transaction.Transaction
import uk.co.randomcoding.partsdb.lift.model.document.DocumentDataHolder
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet._
import uk.co.randomcoding.partsdb.lift.util.SnippetDisplayHelpers._
import uk.co.randomcoding.partsdb.lift.util._
import net.liftweb.common.Full
import net.liftweb.http.js.JsCmds.{ SetHtml, Noop }
import net.liftweb.http.js.JsCmd.unitToJsCmd
import net.liftweb.http.js.JsCmd
import net.liftweb.http.{ StatefulSnippet, S }
import net.liftweb.util.Helpers._
import net.liftweb.util.IterableConst.itNodeSeqFunc
import uk.co.randomcoding.partsdb.lift.util.snippet.display.DocumentTotalsDisplay
import uk.co.randomcoding.partsdb.core.document.Order
import uk.co.randomcoding.partsdb.lift.util.snippet.display.QuoteHolderDocumentTotalsDisplay

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddEditOrder extends StatefulSnippet with ErrorDisplay with DataValidation with QuoteHolderDocumentTotalsDisplay with SubmitAndCancelSnippet with LineItemSnippet {

  override val cameFrom = S.referer openOr "/app/"

  override val quoteHolder = new DocumentDataHolder

  private var customerPoRef = ""
  private var confirmCloseQuote = false

  //private var selectedItems = List.empty[LineItem]

  private[this] val transaction = S.param("transactionId") match {
    case Full(id) => Transaction findById new ObjectId(id)
    case _ => None
  }

  private[this] val (quote, orders) = transaction match {
    case Some(t) => {
      val docs = t.documents.get map (Document findById _) filter (_ isDefined) map (_.get)
      val order = docs find (_.documentType.get == DocumentType.Quote)
      (order, docs filter (_.documentType.get == DocumentType.Order))
    }
    case _ => (None, Seq.empty)
  }

  private[this] val (carriage, lineItems, quoteId) = quote match {
    case Some(q) => {
      val orderedItems = orders flatMap (_.lineItems.get)
      quoteHolder.carriage(q.carriage.get)
      (q.carriage.get, q.lineItems.get filterNot (orderedItems contains _) sortBy (_.lineNumber.get), q.documentNumber)
    }
    case _ => (0.0d, List.empty, "No Quote")
  }

  private[this] val (transactionName, customerName) = transaction match {
    case Some(t) => (t.shortName.get, customerNameFromTransaction(t))
    case _ => ("No Transaction", "No Transaction")
  }

  private[this] def customerNameFromTransaction(t: Transaction) = Customer findById t.customer.get match {
    case Some(c) => c.customerName.get
    case _ => "No Customer for id %s in transaction %s".format(t.customer.get, t.shortName.get)
  }

  private[this] def validateQuoteCloseConfirmation = if (confirmCloseQuote) Nil else Seq("Please confirm it is ok to close the Quote before generating this Order")

  private[this] def performValidation: Seq[String] = validate(validationItems: _*) ++ validateQuoteCloseConfirmation

  override def processSubmit(): JsCmd = {
    performValidation match {
      case Nil => {
        // create order
        val order = Document.add(Order(quoteHolder.lineItems, quoteHolder.carriageValue))
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
      "#availableLineItems *" #> renderAvailableLineItems(lineItems) &
      renderAllLineItems() &
      //"#lineItems" #> LineItemDisplay(selectedItems, false, false) &
      "#quoteId" #> Text(quoteId) &
      "#confirmCloseQuote" #> styledCheckbox(false, confirmCloseQuote = _) &
      renderSubmitAndCancel()
  }

  private[this] def validationItems: Seq[ValidationItem] = Seq(ValidationItem(customerPoRef, "Customer P/O Reference"),
    ValidationItem(quoteHolder.lineItems, "Selected Line Items"))

  private[this] def renderAvailableLineItems(lines: Seq[LineItem]) = lines map (line => {
    val partName = Part findById line.partId.get match {
      case Some(p) => p.partName.get
      case _ => "No Part"
    }

    "#selected" #> styledAjaxCheckbox(false, checkBoxSelected(_, line)) &
      "#partName" #> Text(partName) &
      "#partQuantity" #> Text("%d".format(line.quantity.get)) &
      "#totalLineCost" #> Text("£%.2f".format(line.lineCost))
  })

  private[this] def checkBoxSelected(selected: Boolean, line: LineItem) = {
    selected match {
      case true => {
        quoteHolder.carriage(carriage)
        quoteHolder.addLineItem(line)
      }
      case false => {
        quoteHolder.carriage(0)
        quoteHolder.removeLineItem(line)
      }
    }
    refreshLineItemDisplay()
    //updateSelectedItems()// & refreshTotals()
  }

  //private[this] def updateSelectedItems(): JsCmd = SetHtml("lineItems", LineItemDisplay(selectedItems, false, false))
}