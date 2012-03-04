/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet.display

import scala.xml.{ Text, Null, NodeSeq, Attribute }
import org.bson.types.ObjectId
import org.joda.time.DateTime
import uk.co.randomcoding.partsdb.core.document.DocumentType.{ Quote, Order, Invoice, DocType, DeliveryNote }
import uk.co.randomcoding.partsdb.core.transaction.Transaction
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet._
import net.liftweb.common.{ Logger, Full }
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.http.S
import net.liftweb.util.Helpers._
import uk.co.randomcoding.partsdb.lift.util.TransactionSummaryDisplay
import uk.co.randomcoding.partsdb.lift.util.QuoteDisplay
import uk.co.randomcoding.partsdb.core.document.Document

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object DisplayTransaction extends TabDisplaySnippet with Logger {

  override val tabTitles = Seq(("quoteResults", "Quotes"), ("orderResults", "Orders"), ("invoiceResults", "Invoiced"), ("deliveryNoteResults", "Delivered"))

  private val cameFrom = S.referer openOr "/app/show?entityType=Customer"

  private var currentDocumentType: Option[DocType] = None

  private val transaction = S.param("id") match {
    case Full(id) => Transaction.findById(new ObjectId(id))
    case _ => None
  }

  def render = {
    "#formTitle" #> Text(transaction match {
      case Some(t) => t.shortName.get
      case _ => "No Transaction"
    }) &
      "#quoteLink" #> styledAjaxButton("Quotes", () => displayDocuments(Some(Quote))) &
      "#orderLink" #> styledAjaxButton("Orders", () => displayDocuments(Some(Order))) &
      "#deliveryLink" #> styledAjaxButton("Deliveries", () => displayDocuments(Some(DeliveryNote))) &
      "#invoiceLink" #> styledAjaxButton("Invoiced", () => displayDocuments(Some(Invoice))) &
      "#completedLink" #> styledAjaxButton("Completed", () => displayDocuments(None)) &
      generateDocumentDisplays()
  }

  private def generateDocumentDisplays() = {
    transaction match {
      case None => "#documentTabs" #> generateTabs()
      case Some(t) => {
        val documents = t.documents.get map (Document.findById(_)) filter (_ isDefined) map (_.get)
        "#documentTabs" #> generateTabs() &
          "#quotes *" #> QuoteDetailDisplay(documents filter (_.documentType.get == Quote)) &
          "#orders *" #> OrderDetailDisplay(documents filter (_.documentType.get == Order)) &
          "#deliveryNotes" #> DeliveryNoteDetailDisplay(documents filter (_.documentType.get == DeliveryNote)) &
          "#invoices *" #> InvoiceDetailDisplay(documents filter (_.documentType.get == Invoice))
      }
    }
  }

  private val displayDocuments = (documentType: Option[DocType]) => {
    currentDocumentType = documentType
    SetHtml("documentDisplay", renderCurrentDocument())
  }

  private def renderCurrentDocument(): NodeSeq = currentDocumentType match {
    case Some(docType) => {
      val embedType = "_%s_display".format(docType.toString.toLowerCase)
      <lift:embed/> % Attribute("what", Text(embedType), Null)
    }
    case None => {
      transaction match {
        case Some(t) if (t.transactionState == "Completed") => Text("Transaction completed on: %s".format(new DateTime(t.completionDate.get).toString("dd/MM/yyyy")))
        case Some(t) => {
          currentDocumentType = docTypeForTransactionStage()
          renderCurrentDocument()
        }
        case _ => Text("No Transaction Loaded")
      }
    }
  }

  private def docTypeForTransactionStage() = transaction match {
    case None => {
      error("No Transaction")
      None
    }
    case Some(t) => t.transactionState match {
      case "Completed" => None
      case "Quoted" => Some(Quote)
      case "Ordered" => Some(Order)
      case "Invoiced" => Some(Invoice)
      case other => {
        error("Unknown transaction state: %s".format(other))
        None
      }
    }
  }
}