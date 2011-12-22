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
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle

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
   *
   *     // <td>{ displayVehicle(part) }</td>
   */
  override def displayEntity(part: Part): NodeSeq = {
    <td>{ part.partName }</td>
    <td>{ part.partCost }</td>
    <td>{ part.vehicle.vehicleName }</td>
    ++
    editEntityCell(editEntityLink("Part", part.id))
  }

  private[this] def displayVehicle(part: Part) = {
    debug("Displaying Vehicle for Part: %s".format(part))
    val vehicle = getOne[Vehicle]("vehicleId", part.vehicle.vehicleId.id)
    vehicle match {
      case v: Vehicle => {
        val vehicleLines = Source.fromString(v.vehicleName).getLines()
        <span>{ vehicleLines map (line => <span>{ line }</span><br/>) }</span>
      }
      case _ => Text("Unknown Vehicle. Identifier: %d".format(part.vehicle.vehicleId)
    }
  }

}