/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util

import net.liftweb.http.SHtml.link
import uk.co.randomcoding.partsdb.core.id.Identifier
import scala.xml.Text
import scala.xml.NodeSeq
import org.bson.types.ObjectId

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait EntityDisplay {

  type EntityType

  final val emptyRow = <tr/>

  def displayTable(entities: List[EntityType]): NodeSeq = {
    <table class="btn">
      <thead>
        <tr>{ headings(rowHeadings) }</tr>
      </thead>
      <tbody>
        { entities map (entity => tableRow(displayEntity(entity))) }
      </tbody>
    </table>
  }

  private val tableRow = (dataNodes: NodeSeq) => <tr valign="top">{ dataNodes }</tr>

  def editEntityCell(editNodes: NodeSeq) = <td style="align: right; width: 3em">{ editNodes }</td>
  /**
   * Convert a list of strings into a list to `<th>` elements.
   *
   * This also adds an extra column for the edit button that is put on the end of each row
   */
  private def headings(titles: List[String]) = (titles ::: "" :: Nil) map (title => <th>{ title }</th>)

  /**
   * Create the link to display the edit button for the entity
   */
  val editEntityLink = (entityType: String, entityId: ObjectId) => link("%s?id=%s".format(entityType toLowerCase, entityId.toString), () => Unit, Text("Edit"), "class" -> "btn")

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
  def displayEntity(entity: EntityType): NodeSeq
}

object EntityDisplay {
  val emptyTable = <table>
                     <thead>
                       <tr></tr>
                     </thead>
                     <tbody>
                     </tbody>
                   </table>
}