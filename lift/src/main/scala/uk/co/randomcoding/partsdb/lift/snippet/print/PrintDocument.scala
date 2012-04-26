/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet.print

import scala.io.Source
import scala.xml.{ Text, NodeSeq }
import org.bson.types.ObjectId
import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.document.DocumentType.{ Quote, Order, Invoice, DeliveryNote }
import uk.co.randomcoding.partsdb.core.document.{ LineItem, DocumentType, Document }
import uk.co.randomcoding.partsdb.core.part.Part
import uk.co.randomcoding.partsdb.core.util.MongoHelpers._
import uk.co.randomcoding.partsdb.core.util.CountryCodes
import uk.co.randomcoding.partsdb.lift.util.DateHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet.display.DocumentTotalsDisplay
import net.liftweb.common.Full
import net.liftweb.http.{ StatefulSnippet, S }
import net.liftweb.util.Helpers._
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import net.liftweb.common.Logger

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class PrintDocument extends StatefulSnippet with DocumentTotalsDisplay with Logger {

  private[this] val titleForDocumentType = Map(Quote -> "Quoted", Order -> "Ordered", DeliveryNote -> "Delivered", Invoice -> "Invoiced").withDefaultValue("Unknown Document Type")

  private[this] val document = S param "documentId" match {
    case Full(docId) => Document findById docId
    case _ => None
  }

  private[this] var currentDocument = document

  private[this] var documentNotes = ""

  def dispatch = {
    case "render" => render
  }

  def render = {
    currentDocument match {
      case Some(doc) => renderDocument(doc)
      case _ => renderNoDocument
    }
  }

  private[this] def renderNoDocument = {
    "#id" #> "Stuff"
  }

  private[this] def renderDocument(doc: Document) = {
    renderDocumentHeader(doc) &
      renderPartCostTitle(doc) &
      renderDocumentLineItems(doc.lineItems.get, doc.documentType.get == DocumentType.DeliveryNote) &
      renderDocTotals(doc) &
      renderNotesEntry(doc)
  }

  private[this] def renderNotesEntry(doc: Document) = {
    documentNotes = doc.documentPrintNotes.get

    "#notesEntry" #> styledAjaxTextArea(documentNotes, updateAjaxValue[String](notes => {
      documentNotes = notes
      currentDocument = Document.updateNotes(doc, notes)
      debug("Current Document is now: %s".format(currentDocument))
    }))
  }

  private[this] def renderDocTotals(doc: Document) = {
    if (doc.documentType.get == DocumentType.DeliveryNote) hideDeliveryTotals else renderDocumentTotals(doc)
  }

  private[this] def hideDeliveryTotals = "#displayoftotals" #> <div hidden="true">&nbsp;</div>

  private[this] def renderDocumentHeader(doc: Document) = {
    "#documentAddress" #> addressDisplay(doc.documentAddress.get) &
      "#documentNumber" #> Text(doc.documentNumber) &
      "#documentItemsTitle" #> Text("%s Items".format(titleForDocumentType(doc.documentType.get))) &
      "#documentDate" #> Text(dateString(doc.createdOn.get))
  }

  private[this] def renderPartCostTitle(doc: Document) = {
    "#lineCostTitle" #> (doc.documentType.get match {
      case DeliveryNote => Text("")
      case _ => Text("Line Cost")
    })
  }

  private[this] def renderDocumentLineItems(lineItems: Seq[LineItem], isDeliveryNote: Boolean) = {
    "#lineItems *" #> (lineItems map (renderLineItem(_, isDeliveryNote)))
  }

  private[this] def renderLineItem(lineItem: LineItem, isDeliveryNote: Boolean) = {
    "#lineNumber" #> Text("%d".format(lineItem.lineNumber.get)) &
      "#partName" #> Text(nameForPart(lineItem.partId.get)) &
      "#partQuantity" #> Text("%d".format(lineItem.quantity.get)) &
      "#partCost" #> (if (isDeliveryNote) Text("") else Text("£%.2f".format(lineItem.lineCost)))
  }

  private[this] def nameForPart(partId: ObjectId) = {
    Part findById partId match {
      case Some(p) => p.partName.get
      case _ => "No part with id: %s".format(partId.toString)
    }
  }

  private[this] def addressDisplay(address: Address): Seq[NodeSeq] = {
    val addressSource = Source.fromString(address.addressText.get).getLines.toSeq

    addressSource map (line => CountryCodes.matchToCountryCode(line) match {
      case None => Text(line) ++ <br/>
      case Some(_) => Text(line)
    })
  }
}