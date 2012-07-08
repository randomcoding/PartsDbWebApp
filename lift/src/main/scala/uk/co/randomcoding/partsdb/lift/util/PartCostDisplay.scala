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
package uk.co.randomcoding.partsdb.lift.util

import scala.xml.NodeSeq

import org.joda.time.DateTime

import uk.co.randomcoding.partsdb.core.part.{ PartCost, Part }
import uk.co.randomcoding.partsdb.lift.util._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object PartCostDisplay extends TabularEntityDisplay {

  override val addEditColumn = false
  override val addDisplayColumn = false
  override type EntityType = PartCost

  override val rowHeadings = List("Part", "Cost", "Last Updated", "Supplier Part No.")

  def displayEntity(entity: PartCost, editLink: Boolean = false, displayLink: Boolean = false): NodeSeq = {
    <td>{ displayPartName(entity) }</td>
    <td>{ "£%.2f".format(entity.suppliedCost.get) }</td>
    <td>{ displayDate(entity) }</td>
    <td>{ entity.supplierPartNumber.get }</td> //++ emptyEditAndDisplayCells
  }

  private def displayPartName(partCost: PartCost) = {
    Part findById partCost.part.get match {
      case Some(p) => p.partName.get
      case _ => "Unknown Part Id: %s".format(partCost.part.get)
    }
  }

  private def displayDate(partCost: PartCost) = {
    val date = partCost.lastSuppliedDate.get
    new DateTime(date).toString("dd/MM/yyyy")
  }
}
