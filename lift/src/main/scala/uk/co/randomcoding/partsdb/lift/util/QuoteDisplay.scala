/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util

import scala.xml.NodeSeq

import uk.co.randomcoding.partsdb.core.document.Document
import uk.co.randomcoding.partsdb.lift.util._

import net.liftweb.common.Logger

/**
 * Helper functions for displaying quotes in lift pages
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object QuoteDisplay extends TabularEntityDisplay with Logger {
  type EntityType = Document
  /**
   * The headings to use for the display of the customer data table
   */
  override val rowHeadings = List("Quote Number")

  /**
   * Generates html to display a Quote.
   *
   * Currently displays the Quote Document Number
   *
   * @param doc The [[uk.co.randomcoding.partsdb.core.document.Document]] to display
   *
   * @return A [[scala.xml.NodeSeq]] of `<td>` elements to display the quote details
   */
  override def displayEntity(doc: Document, editLink: Boolean = false, displayLink: Boolean = false): NodeSeq = {
    <td>{ doc.documentNumber }</td> ++
      emptyEditAndDisplayCells
  }
}