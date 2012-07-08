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
package uk.co.randomcoding.partsdb.lift.util

import scala.xml.{ Text, NodeSeq }
import org.bson.types.ObjectId
import net.liftweb.http.SHtml._
import scala.xml.Attribute
import scala.xml.Null
import net.liftweb.common.Logger

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait TabularEntityDisplay extends Logger {

  type EntityType

  val addEditColumn = true
  val addDisplayColumn = true

  final val emptyRow = <tr/>

  /**
   * Convenience apply method that generates the html table for the entities
   */
  def apply(entities: Seq[EntityType], editLink: Boolean = true, displayLink: Boolean = true): NodeSeq = displayTable(entities, editLink, displayLink)

  private def displayTable(entities: Seq[EntityType], editLink: Boolean = true, displayLink: Boolean = true): NodeSeq = {
    debug("Rendering Table for %s".format(entities.mkString("[", ", ", "]")))
    <table class="btn">
      <thead>
        <tr>{ headings(rowHeadings) }</tr>
      </thead>
      <tbody>
        { entities map (entity => tableRow(displayEntity(entity, editLink, displayLink))) }
      </tbody>
    </table>
  }

  def addEditAndDisplayCells(entityType: String, entityId: ObjectId) = tableCell(editEntityLink(entityType, entityId)) ++ tableCell(displayEntityLink(entityType, entityId))

  def addEditCellOnly(entityType: String, entityId: ObjectId) = tableCell(editEntityLink(entityType, entityId), 2)

  def addDisplayCellOnly(entityType: String, entityId: ObjectId) = tableCell(displayEntityLink(entityType, entityId), 2)

  def editAndDisplayCells(entityType: String, entityId: ObjectId, editLink: Boolean, displayLink: Boolean): NodeSeq = (editLink, displayLink) match {
    case (true, true) => addEditAndDisplayCells(entityType, entityId)
    case (true, false) => addEditCellOnly(entityType, entityId)
    case (false, true) => addDisplayCellOnly(entityType, entityId)
    case (false, false) => emptyEditAndDisplayCells
  }

  val emptyEditAndDisplayCells = tableCell(Text("")) ++ tableCell(Text(""))

  private val tableRow = (dataNodes: NodeSeq) => <tr valign="top">{ dataNodes }</tr>

  /**
   * Create a Table cell containing a provided node sequence
   *
   * @param content The html to display in the table cell
   * @param colspan The number of columns this table cell should span. Defaults to 1
   * @param rowspan The number of rows this table cell should span. Defaults to 1
   */
  def tableCell(content: NodeSeq, colspan: Int = 1, rowspan: Int = 1) = {
    <td style="align: right;">{ content }</td> %
      Attribute("colspan", Text("%d".format(colspan)), Null) %
      Attribute("rowspan", Text("%d".format(rowspan)), Null)
  }

  /**
   * Convert a list of strings into a list to `<th>` elements.
   *
   * This also adds an extra column for the edit button and the display button that are put on the end of each row
   */
  private def headings(titles: List[String]) = {
    val headings = List(addEditColumn, addDisplayColumn) count (_ == true) match {
      case 2 => (titles ::: "" :: "" :: Nil)
      case 1 => (titles ::: "" :: Nil)
      case 0 => titles
    }

    headings map (title => <th>{ title }</th>)
  }

  /**
   * Create the link to display the edit button for the entity
   */
  val editEntityLink = (entityType: String, entityId: ObjectId) => link("%s?id=%s".format(entityType toLowerCase, entityId.toString), () => Unit, Text("Edit"), "class" -> "btn", "style" -> "width: 4em")

  /**
   * Create the link to display the Display button for the entity
   */
  val displayEntityLink = (entityType: String, entityId: ObjectId) => link("/app/display/%s?id=%s".format(entityType toLowerCase, entityId toString), () => Unit, Text("Display"), "class" -> "btn", "style" -> "width: 5em")

  /**
   * This is the list of heading to be displayed for the entity table
   *
   * This will have an '''Edit''' entry appended to it
   */
  val rowHeadings: List[String]

  /**
   * Generate the `<td>` elements to display the entity
   *
   * This is also responsible for creating the edit entity link for the additional row that is added
   */
  def displayEntity(entity: EntityType, editLink: Boolean, displayLink: Boolean): NodeSeq
}

object TabularEntityDisplay {
  val emptyTable = <table>
                     <thead>
                       <tr></tr>
                     </thead>
                     <tbody>
                     </tbody>
                   </table>
}
