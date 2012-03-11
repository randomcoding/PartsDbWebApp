/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import uk.co.randomcoding.partsdb.lift.util.snippet._
import net.liftweb.http.StatefulSnippet
import net.liftweb.http.js.JsCmds.Noop
import uk.co.randomcoding.partsdb.lift.model.document.QuoteHolder
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.S
import net.liftweb.util.Helpers._
import net.liftweb.common.Full
import uk.co.randomcoding.partsdb.core.transaction.Transaction
import org.bson.types.ObjectId
import scala.xml.Text
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.document.Document
import uk.co.randomcoding.partsdb.core.document.DocumentType
import uk.co.randomcoding.partsdb.core.document.LineItem
import uk.co.randomcoding.partsdb.lift.util.LineItemDisplay
import net.liftweb.http.js.JsCmds.SetHtml
import uk.co.randomcoding.partsdb.core.part.Part

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddEditOrder extends StatefulSnippet with ErrorDisplay with DataValidation with SubmitAndCancelSnippet with LineItemSnippet {

  override val cameFrom = S.referer openOr "/app/"

  override val quoteHolder = new QuoteHolder

  private var customerPoRef = ""
  private var confirmCloseQuote = false

  private var selectedItems = List.empty[LineItem]

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

  private[this] val (carriageText, lineItems) = quote match {
    case Some(q) => {
      val orderedItems = orders flatMap (_.lineItems.get)
      ("£%.2f".format(q.carriage.get), q.lineItems.get filterNot (orderedItems contains _) sortBy (_.lineNumber.get))
    }
    case _ => ("No Quote", List.empty)
  }

  private[this] val (transactionName, customerName) = transaction match {
    case Some(t) => (t.shortName.get, customerNameFromTransaction(t))
    case _ => ("No Transaction", "No Transaction")
  }

  private[this] def customerNameFromTransaction(t: Transaction) = Customer findById t.customer.get match {
    case Some(c) => c.customerName.get
    case _ => "No Customer for id %s in transaction %s".format(t.customer.get, t.shortName.get)
  }

  override def processSubmit(): JsCmd = {
    validate(validationItems: _*) match {
      case Nil => {
        confirmCloseQuote match {
          case false => {
            displayError("Please confirm it is ok to close the Quote before generating this Order")
            Noop
          }
          case true => {
            // create order
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
      "#carriage" #> Text(carriageText) &
      "#customerPoRefEntry" #> styledText(customerPoRef, customerPoRef = _) &
      "#availableLineItems *" #> renderLineItems(lineItems) &
      "#selectedItems" #> LineItemDisplay(selectedItems, false, false) &
      "#confirmCloseQuote" #> (Text("Confirm you wish to close Quote: %s") ++ styledCheckbox(false, confirmCloseQuote = _))
  }

  private[this] def validationItems: Seq[ValidationItem] = Seq(ValidationItem(customerPoRef, "Customer P/O Reference"),
    ValidationItem(selectedItems, "Selected Line Items"))

  private[this] def renderLineItems(lines: Seq[LineItem]) = lines map (line => {
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
      case true => selectedItems = line :: lineItems
      case false => selectedItems = lineItems filterNot (_.lineNumber.get == line.lineNumber.get)
    }
    updateSelectedItems()
  }

  private[this] def updateSelectedItems(): JsCmd = SetHtml("selectedItems", LineItemDisplay(selectedItems, false, false))
}