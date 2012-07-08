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
    "#printDocument" #> buttonLink("Print Preview Document", printTarget(document), attrs = List("target" -> "_blank"))
  }
}
