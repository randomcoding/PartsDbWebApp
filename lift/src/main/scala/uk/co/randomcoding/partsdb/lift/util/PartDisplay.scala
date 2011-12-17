/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util

import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmds.Noop
import scala.xml.NodeSeq
import net.liftweb.common.Logger
import uk.co.randomcoding.partsdb.lift.util.snippet.DbAccessSnippet
import uk.co.randomcoding.partsdb.core.part.Part

import scala.xml.Text
import uk.co.randomcoding.partsdb.db.DbAccess
import scala.io.Source

/**
 * Helper functions for displaying parts in lift pages
 *
 * @author Jane Rowe
 */
object PartDisplay extends EntityDisplay with Logger with DbAccessSnippet {
  type EntityType = Part
  /**
   * The headings to use for the display of the part data table
   */
  override val rowHeadings = List("Part Name", "Cost")

  /**
   * Generates html to display a part.
   *
   * Currently displays the part name, and cost
   *
   * @param part The [[uk.co.randomcoding.partsdb.core.part.Part]] to display
   * @return A [[scala.xml.NodeSeq]] of `<td>` elements to display the part details
   */
  override def displayEntity(part: Part): NodeSeq = {
    <td>{ part.partName }</td>
    <td>{ part.partCost }</td>
    ++
    editEntityCell(editEntityLink("Part", part.id))
  }

}