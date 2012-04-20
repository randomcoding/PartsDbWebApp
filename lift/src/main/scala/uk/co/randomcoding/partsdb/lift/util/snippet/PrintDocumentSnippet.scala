/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet

import scala.xml.Text

import uk.co.randomcoding.partsdb.core.document.DocumentType.{ Quote, Order, Invoice, DeliveryNote }
import uk.co.randomcoding.partsdb.core.document.Document
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._

import net.liftweb.http.SHtml._
import net.liftweb.util.Helpers._

/**
 * Snippet to render a button to open a new window to display a printable version of a document.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait PrintDocumentSnippet {

  private[this] def printTarget(document: Document): String = "/app/print/printdocument?documentId=%s".format(document.id.get)

  def renderPrintDocument(document: Document) = {
    "#printDocument" #> link(printTarget(document), noopFunction, Text("Print Preview Document"), ("target" -> "_blank"))
  }
}