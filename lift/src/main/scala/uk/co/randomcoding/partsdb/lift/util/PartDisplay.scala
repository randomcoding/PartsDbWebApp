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
