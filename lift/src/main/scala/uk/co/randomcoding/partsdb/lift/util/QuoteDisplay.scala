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
