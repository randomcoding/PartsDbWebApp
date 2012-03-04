/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util

import scala.xml.{ Text, NodeSeq }

import org.bson.types.ObjectId

import uk.co.randomcoding.partsdb.core.part.Part
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle
import uk.co.randomcoding.partsdb.lift.util._

import net.liftweb.common.Logger

/**
 * Helper functions for displaying parts in lift pages
 *
 * @author Jane Rowe
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object PartDisplay extends TabularEntityDisplay with Logger {
  type EntityType = Part
  /**
   * The headings to use for the display of the part data table
   */
  override val rowHeadings = List("Part Name", "Vehicles", "MoD IDs")

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
  override def displayEntity(part: Part, editLink: Boolean, displayLink: Boolean): NodeSeq = {
    <td>{ part.partName.get }</td>
    <td>{ vehicleName(part.vehicle.get) }</td>
    <td>{
      part.modId.get match {
        case Some(mid) => mid
        case _ => ""
      }
    }</td> ++
      editAndDisplayCells("Part", part.id.get, editLink, displayLink)
  }

  private[this] def vehicleName(vehicleId: ObjectId) = Vehicle findById vehicleId match {
    case Some(vehicle) => vehicle.vehicleName.get
    case _ => "No Vehicle"
  }

  private[this] def displayVehicle(part: Part) = {
    debug("Displaying Vehicle for Part: %s".format(part))
    /*part.vehicles match {
      case Some(v) => {
        val vehicleLines = Source.fromString(v.vehicleName).getLines()
        <span>{ vehicleLines map (line => <span>{ line }</span><br/>) }</span>
      }
      case _ =>*/ Text("Unspecified Vehicle.")
    //}
  }
}
