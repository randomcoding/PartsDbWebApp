/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util

import scala.xml.{ Text, NodeSeq }
import org.bson.types.ObjectId
import net.liftweb.http.SHtml._
import scala.xml.Attribute
import scala.xml.Null

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait TabularEntityDisplay {

  type EntityType

  val addEditColumn = true
  val addDisplayColumn = true

  final val emptyRow = <tr/>

  /**
   * Convenience apply method that generates the html table for the entities
   */
  def apply(entities: List[EntityType], editLink: Boolean = true, displayLink: Boolean = true): NodeSeq = displayTable(entities, editLink, displayLink)

  private def displayTable(entities: List[EntityType], editLink: Boolean = true, displayLink: Boolean = true): NodeSeq = {
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
    <td style="align: right; width: 3em">{ content }</td> %
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
  val editEntityLink = (entityType: String, entityId: ObjectId) => link("%s?id=%s".format(entityType toLowerCase, entityId.toString), () => Unit, Text("Edit"), "class" -> "btn")

  /**
   * Create the link to display the Display button for the entity
   */
  val displayEntityLink = (entityType: String, entityId: ObjectId) => link("/app/display/%s?id=%s".format(entityType toLowerCase, entityId toString), () => Unit, Text("Display"), "class" -> "btn")

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