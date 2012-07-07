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
package uk.co.randomcoding.partsdb.lift.snippet.display

import scala.xml.{ Text, NodeSeq }

import com.foursquare.rogue.Rogue._

import uk.co.randomcoding.partsdb.core.part.Part
import uk.co.randomcoding.partsdb.core.supplier.Supplier
import uk.co.randomcoding.partsdb.core.util.MongoHelpers._
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._

import net.liftweb.common.Full
import net.liftweb.http.SHtml.ElemAttr.pairToBasic
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.http.{ StatefulSnippet, S }
import net.liftweb.util.Helpers._

/**
 * This snippet displays the details for a vehicle record.
 *
 * Shown is the Vehicle's name, MoD Id and details of the
 * parts that are supplier for this vehicle.
 *
 * It relies on a request parameter of `id` being provided with the
 * object id of a valid [[uk.co.randomcoding.partsdb.core.part.Part]]
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class DisplayVehicle extends StatefulSnippet {

  private[this] val vehicle = S param "id" match {
    case Full(id) => Vehicle.findById(id)
    case _ => None
  }

  private[this] lazy val vehicleName = vehicle match {
    case Some(v) => v.vehicleName.get
    case _ => "No Vehicle Found"
  }

  def dispatch = {
    case "render" => render
  }

  def render = {
    "#formTitle" #> Text("Displaying: %s".format(vehicleName)) &
      "#vehicleParts *" #> renderVehicleParts(partsForVehicle) &
      "#vehiclePdf" #> renderVehiclePdf
  }

  private[this] def partsForVehicle: List[Part] = vehicle match {
    case Some(v) => Part.where(_.vehicle eqs v.id.get).fetch
    case _ => Nil
  }

  private[this] def renderVehicleParts(parts: List[Part]) = parts map (part => {
    "#vehiclePartName" #> Text(part.partName.get) &
      "#vehiclePartMoDId" #> Text(part.modId.get match {
        case Some(id) => id
        case _ => "No MoD Id"
      }) &
      "#vehiclePartSuppliers *" #> suppliersInfoForPart(part)
  })

  private[this] def suppliersInfoForPart(part: Part): Seq[NodeSeq] = {
    val suppliers = Supplier.where(_.suppliedParts.subfield(_.part) eqs part.id.get).orderAsc(_.supplierName).fetch

    suppliers match {
      case Nil => Seq(span(Text("No Suppliers for part: %s".format(part.partName.get)), Noop))
      case s => suppliers map (supplierInfoForPart(_, part))
    }
  }

  private[this] def supplierInfoForPart(supplier: Supplier, part: Part): NodeSeq = {
    val supplierPartCost = supplier.suppliedParts.get.filter(_.part.get == part.id.get).headOption
    val partId = supplierPartCost match {
      case Some(pc) => pc.supplierPartNumber.get
      case _ => "Not Supplied" // should not see this
    }
    val partCost = supplierPartCost match {
      case Some(pc) => "£%.2f".format(pc.suppliedCost.get)
      case _ => "Not Supplied" // should not see this
    }

    // TODO: Change link target to /app/display/supplier?id= when supplier display is completed
    plainLink("%s; Part Id: %s; Part Cost: %s".format(supplier.supplierName.get, partId, partCost), "/app/supplier?id=%s".format(supplier.id.get), attrs = List("style" -> "font-weight: normal"))
  }

  private[this] val supplierEntrySeparator = Text("; ")

  private[this] def renderVehiclePdf = vehicle match {
    case Some(v) => v.pdfFile.get.trim match {
      case "" => noPdfFile
      case fileName => Text(fileName)
    }
    case _ => noPdfFile
  }

  private[this] val noPdfFile = Text("No Pdf File Available")

}