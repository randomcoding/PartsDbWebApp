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

import net.liftweb.http.StatefulSnippet
import net.liftweb.http.S
import net.liftweb.http.SHtml.span
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle
import uk.co.randomcoding.partsdb.core.util.MongoHelpers._
import net.liftweb.common.Full
import net.liftweb.util.Helpers._
import net.liftweb.http.js.JsCmds.Noop
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import scala.xml.Text
import uk.co.randomcoding.partsdb.core.system.SystemData
import uk.co.randomcoding.partsdb.core.part.Part
import com.foursquare.rogue.Rogue._
import net.liftweb.util.CssSel
import uk.co.randomcoding.partsdb.core.supplier.Supplier

/**
 * This snippet displays the details for a
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
      "#vehiclePartSuppliers" #> renderSuppliersForPart(Supplier.where(_.suppliedParts.subfield(_.part) eqs part.id.get).orderAsc(_.supplierName).fetch, part)
  })

  private[this] val supplierEntrySpaces = Text("    ")

  private[this] def renderSuppliersForPart(suppliers: List[Supplier], part: Part) = suppliers map (supplier => {
    val nameNode = Text(supplier.supplierName.get)
    val supplierPartCost = supplier.suppliedParts.get.filter(_.part.get == part.id.get).headOption
    val partIdNode = supplierPartCost match {
      case Some(pc) => Text("Part Id: %s".format(pc.supplierPartNumber.get))
      case _ => Text("Not Supplied") // should not see this
    }
    val partCostNode = supplierPartCost match {
      case Some(pc) => Text("Part Price: £%.2f".format(pc.suppliedCost.get))
      case _ => Text("Not Supplied") // should not see this
    }

    span(nameNode ++ supplierEntrySpaces ++ partIdNode ++ supplierEntrySpaces ++ partCostNode, Noop)
  })

  private[this] def renderVehiclePdf = vehicle match {
    case Some(v) => v.pdfFile.get.trim match {
      case "" => noPdfFile
      case fileName => buttonLink("Display Pdf File", "file://%s/%s".format(SystemData.vehiclePdfPath, fileName), noopFunction)
    }
    case _ => noPdfFile
  }

  private[this] val noPdfFile = Text("No Pdf File Available")

}