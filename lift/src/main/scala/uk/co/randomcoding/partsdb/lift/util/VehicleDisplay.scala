/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util

import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmds.Noop
import scala.xml.NodeSeq
import net.liftweb.common.Logger
import uk.co.randomcoding.partsdb.core.part.Part
import scala.xml.Text
import scala.io.Source
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle

/**
 * Helper functions for displaying vehicles in lift pages
 *
 * @author Jane Rowe
 */
object VehicleDisplay extends EntityDisplay with Logger {
  type EntityType = Vehicle
  /**
   * The headings to use for the display of the vehicle data table
   */
  override val rowHeadings = List("Vehicle Name")

  /**
   * Generates html to display a vehicle.
   *
   * Currently displays the vehicle name, and cost
   *
   * @param vehicle The [[uk.co.randomcoding.partsdb.core.vehicle.Vehicle]] to display
   * @return A [[scala.xml.NodeSeq]] of `<td>` elements to display the vehicle details
   */
  override def displayEntity(vehicle: Vehicle): NodeSeq = {
    <td>{ vehicle.vehicleName }</td> ++
      editEntityCell(editEntityLink("Vehicle", vehicle.id.get))
  }
}