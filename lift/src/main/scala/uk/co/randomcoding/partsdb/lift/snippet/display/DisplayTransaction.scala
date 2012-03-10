/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet.display

import scala.xml.Text

import org.bson.types.ObjectId

import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.document.DocumentType.{ Quote, DocType }
import uk.co.randomcoding.partsdb.core.document.Document
import uk.co.randomcoding.partsdb.core.transaction.Transaction
import uk.co.randomcoding.partsdb.lift.util.snippet.display.QuoteDetailDisplay
import uk.co.randomcoding.partsdb.lift.util.snippet._

import net.liftweb.common.{ Logger, Full }
import net.liftweb.http.SHtml._
import net.liftweb.http.S
import net.liftweb.util.Helpers._

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
      "#backLink" #> link(cameFrom, () => (), Text("Back...")) &
      "#customerName" #> Text(transaction match {
        case Some(t) => Customer.findById(t.customer.get) match {
          case Some(c) => c.customerName.get
          case _ => "No Customer"
        }
        case _ => "No Transaction"
      }) &
      generateDocumentDisplays()
  }

  private def generateDocumentDisplays() = {
    transaction match {
      case None => "#documentTabs" #> generateTabs()
      case Some(t) => {
        val documents = t.documents.get map (Document.findById(_)) filter (_ isDefined) map (_.get)
        "#documentTabs" #> generateTabs() &
          "#quotes *" #> QuoteDetailDisplay(documents filter (_.documentType.get == Quote)) /*&
          "#orders *" #> OrderDetailDisplay(documents filter (_.documentType.get == Order)) &
          "#deliveryNotes" #> DeliveryNoteDetailDisplay(documents filter (_.documentType.get == DeliveryNote)) &
          "#invoices *" #> InvoiceDetailDisplay(documents filter (_.documentType.get == Invoice))*/
      }
    }
  }
}