/*
 * Copyright (C) 2012 RandomCoder <randomcoder@randomcoding.co.uk>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *    RandomCoder - initial API and implementation and/or initial documentation
 */
package uk.co.randomcoding.partsdb.lift.snippet.print

import scala.io.Source
import scala.xml.{ Text, NodeSeq }

import com.foursquare.rogue.Rogue._

import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.document.DocumentType.{ Quote, Order, Invoice, DeliveryNote }
import uk.co.randomcoding.partsdb.core.document.{ LineItem, DocumentType, Document }
import uk.co.randomcoding.partsdb.core.part.{ PartKit, Part }
import uk.co.randomcoding.partsdb.core.transaction.Transaction
import uk.co.randomcoding.partsdb.core.util.MongoHelpers._
import uk.co.randomcoding.partsdb.core.util.CountryCodes
import uk.co.randomcoding.partsdb.lift.util.DateHelpers._
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet.display.DocumentTotalsDisplay

import net.liftweb.common.{ Logger, Full }
import net.liftweb.http.{ StatefulSnippet, S }
import net.liftweb.util.Helpers._

/**
 * Snippet to display the printable preview of a [[uk.co.randomcoding.partsdb.core.document.Document]]
 *
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
    "#customerName" #> Text(getCustomerNameForDocument(doc)) &
      "#documentAddress" #> addressDisplay(doc.documentAddress.get) &
      "#documentNumber" #> Text(doc.documentNumber) &
      "#documentItemsTitle" #> Text("%s Items".format(titleForDocumentType(doc.documentType.get))) &
      "#documentDate" #> Text(dateString(doc.createdOn.get))
  }

  private[this] def getCustomerNameForDocument(doc: Document): String = {
    Transaction where (_.documents contains doc.id.get) get () match {
      case Some(transaction) => Customer where (_.id eqs transaction.customer.get) get () match {
        case Some(customer) => customer.customerName.get
        case _ => "No Customer for transaction"
      }
      case _ => "No Transaction with Document"
    }
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
    /*"#lineNumber" #> Text("%d".format(lineItem.lineNumber.get) + 1) &*/
    "#partName" #> Text(nameForPartOrKit(lineItem.partId.get)) &
      "#partQuantity" #> Text("%d".format(lineItem.quantity.get)) &
      "#partCost" #> (if (isDeliveryNote) Text("") else Text("£%.2f".format(lineItem.lineCost)))
  }

  private[this] def nameForPartOrKit(partId: String) = {
    Part findById partId match {
      case Some(p) => p.partName.get
      case _ => PartKit.findById(partId) match {
        case Some(pk) => pk.kitName.get
        case _ => "No part with id: %s".format(partId.toString)
      }
    }
  }

  private[this] def addressDisplay(address: Address): Seq[NodeSeq] = {
    val addressLines = Source.fromString(address.addressText.get).getLines.toSeq ++ Seq(address.country.get)

    addressLines map (line => CountryCodes.matchToCountryCode(line) match {
      case None => Text(line + ",") ++ <br/>
      case Some(_) => Text(line)
    })
  }
}
