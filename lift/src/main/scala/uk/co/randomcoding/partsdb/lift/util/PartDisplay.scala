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

/**
 * Helper functions for displaying parts in lift pages
 *
 * @author Jane Rowe
 */
object PartDisplay extends Logger with DbAccessSnippet {
  /**
   * Generates html to display a part.
   *
   * Currently displays the part name, and cost
   *
   * @param part The [[uk.co.randomcoding.partsdb.core.part.Part]] to display
   * @return A [[scala.xml.NodeSeq]] to display the part details
   */
  def displayPart(part: Part): NodeSeq = {
    <tr valign="top">
      <td>{ part.partName }</td>
      <td>{ part.partCost }</td>
    </tr>
  }
}