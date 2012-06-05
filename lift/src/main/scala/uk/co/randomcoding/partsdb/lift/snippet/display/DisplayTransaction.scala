/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet.display

import scala.xml.Text

import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.document.DocumentType.{ Quote, Order, Invoice, DocType, DeliveryNote }
import uk.co.randomcoding.partsdb.core.document.Document
import uk.co.randomcoding.partsdb.core.transaction.Transaction
import uk.co.randomcoding.partsdb.core.util.MongoHelpers._
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet.display.{ QuoteDetailDisplay, OrderDetailDisplay, InvoiceDetailDisplay, DeliveryNoteDetailDisplay }
import uk.co.randomcoding.partsdb.lift.util.snippet._

import net.liftweb.common.{ Logger, Full }
import net.liftweb.http.S
import net.liftweb.util.Helpers._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object DisplayTransaction extends TabDisplaySnippet with Logger {

  override val tabTitles = Seq(("quoteResults", "Quoted"), ("orderResults", "Ordered"), ("deliveryNoteResults", "Delivered"), ("invoiceResults", "Invoiced"))

  /*
   * Form a closure over passed in data to enable the rendering of the correct info
   */
  def render = {
    val cameFrom = S.referer openOr "/app/show?entityType=Customer"

    var currentDocumentType: Option[DocType] = None

    val transaction = S.param("id") match {
      case Full(id) => Transaction.findById(id)
      case _ => None
    }

    def generateDocumentDisplays() = transaction match {
      case None => "#documentTabs" #> generateTabs()
      case Some(t) => {
        val documents = t.documents.get map (Document.findById(_)) filter (_ isDefined) map (_.get)
        debug("Found %s documents".format(documents.mkString(", ")))
        val transactionId = t.id.get.toString
        "#documentTabs" #> generateTabs() &
          "#quotes *" #> QuoteDetailDisplay(documents filter (_.documentType.get == Quote), transactionId) &
          "#orders *" #> OrderDetailDisplay(documents filter (_.documentType.get == Order), transactionId) &
          "#deliveryNotes" #> DeliveryNoteDetailDisplay(documents filter (_.documentType.get == DeliveryNote), transactionId) &
          "#invoices *" #> InvoiceDetailDisplay(documents filter (_.documentType.get == Invoice), transactionId)
      }
    }

    /*
     * Perform actual render of page
     */

    debug("Rendering details for transaction: %s".format(transaction))
    val transactionTitleText = transaction match {
      case Some(t) => t.shortName.get
      case _ => "No Transaction"
    }

    val customerNameText = transaction match {
      case Some(t) => Customer.findById(t.customer.get) match {
        case Some(c) => c.customerName.get
        case _ => "No Customer"
      }
      case _ => "No Transaction"
    }

    "#formTitle" #> Text(transactionTitleText) &
      "#backLink" #> buttonLink(" <- Back", cameFrom) &
      "#customerName" #> Text(customerNameText) &
      generateDocumentDisplays()
  }
}