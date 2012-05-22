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

import uk.co.randomcoding.partsdb.core.part.PartKit

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object PartKitDisplay extends TabularEntityDisplay {
  override type EntityType = PartKit

  override val rowHeadings = List("Kit Name", "Kit Description", "Cost Price", "Total Price")

  override def displayEntity(partKit: PartKit, editLink: Boolean, displayLink: Boolean): NodeSeq = {
    <td>{ partKit.kitName.get }</td>
    <td>{ partKit.description.get }</td>
    <td>{ "£%.2f".format(partKit.costPrice) }</td>
    <td>{ "£%.2f".format(partKit.kitPrice) }</td> ++
      editAndDisplayCells("PartKit", partKit.id.get, editLink, displayLink)
  }
}