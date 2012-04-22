/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet.print

import net.liftweb.http.StatefulSnippet
import net.liftweb.util.Helpers._
import net.liftweb.http.S
import net.liftweb.common.Full
import uk.co.randomcoding.partsdb.core.document.Document
import uk.co.randomcoding.partsdb.core.util.MongoHelpers._
import uk.co.randomcoding.partsdb.core.address.Address
import scala.io.Source
import scala.xml.Text
import scala.xml.NodeSeq
import uk.co.randomcoding.partsdb.core.util.CountryCodes
import uk.co.randomcoding.partsdb.core.document.DocumentType._
import uk.co.randomcoding.partsdb.lift.util.DateHelpers._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class PrintDocument extends StatefulSnippet {

  private[this] val titleForDocumentType = Map(Quote -> "Quote", Order -> "Order", DeliveryNote -> "Delivery Note", Invoice -> "Invoice").withDefaultValue("Unknown Document Type")

  private[this] val document = S param "documentId" match {
    case Full(docId) => Document findById docId
    case _ => None
  }

  def dispatch = {
    case "render" => render
  }

  def render = {
    document match {
      case Some(doc) => renderDocument(doc)
      case _ => renderNoDocument
    }
  }

  private[this] def renderNoDocument = {
    "#id" #> "Stuff"
  }

  private[this] def renderDocument(doc: Document) = {
    // document header
    "#documentAddress" #> addressDisplay(doc.documentAddress.get) &
      "#documentNumber" #> Text(doc.documentNumber) &
      "#documentTypeTitle" #> Text(titleForDocumentType(doc.documentType.get)) &
      "#documentDate" #> Text(dateString(doc.createdOn.get))
  }

  private[this] def addressDisplay(address: Address): Seq[NodeSeq] = {
    val addressSource = Source.fromString(address.addressText.get).getLines.toSeq

    addressSource map (line => CountryCodes.matchToCountryCode(line) match {
      case None => Text(line) ++ <br/>
      case Some(_) => Text(line)
    })
  }
}