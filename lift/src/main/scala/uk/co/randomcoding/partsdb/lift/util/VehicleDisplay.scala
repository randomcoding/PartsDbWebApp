/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util

import scala.xml.NodeSeq

import uk.co.randomcoding.partsdb.core.vehicle.Vehicle
import uk.co.randomcoding.partsdb.lift.util._

import net.liftweb.common.Logger

/**
 * Helper functions for displaying vehicles in lift pages
 *
 * @author Jane Rowe
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
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
  override def displayEntity(vehicle: Vehicle, editLink: Boolean, displayLink: Boolean): NodeSeq = {
    <td>{ vehicle.vehicleName }</td> ++
      editAndDisplayCells("Vehicle", vehicle.id.get, editLink, displayLink)
  }
}