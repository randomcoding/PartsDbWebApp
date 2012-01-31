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
import com.foursquare.rogue.Rogue._
import org.bson.types.ObjectId

/**
 * Helper functions for displaying parts in lift pages
 *
 * @author Jane Rowe
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object PartDisplay extends EntityDisplay with Logger {
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
  override def displayEntity(part: Part): NodeSeq = {
    <td>{ part.partName.get }</td>
    <td>{ vehicleName(part.vehicle.get) }</td>
    <td>{
      part.modId.get match {
        case Some(mid) => mid
        case _ => ""
      }
    }</td> ++
      editEntityCell(editEntityLink("Part", part.id.get))
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
