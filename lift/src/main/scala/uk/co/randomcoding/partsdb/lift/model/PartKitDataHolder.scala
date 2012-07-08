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
package uk.co.randomcoding.partsdb.lift.model

import uk.co.randomcoding.partsdb.core.part.PartKit
import uk.co.randomcoding.partsdb.lift.model.document.NewLineItemDataHolder

import net.liftweb.util.ValueCell

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class PartKitDataHolder extends NewLineItemDataHolder {

  private[this] val partKitName = ValueCell[String]("")

  private[this] val partKitDescription = ValueCell[String]("")

  def kitName: String = partKitName.get

  def kitName_=(newName: String) = partKitName.set(newName)

  def kitDescription: String = partKitDescription.get

  def kitDescription_=(newDescription: String) = partKitDescription.set(newDescription)

  /**
   * Create the part kit from the current data
   */
  def partKit: PartKit = PartKit(kitName, lineItems, kitDescription)

  /**
   * Set the kit name, description and parts from the provided part kit
   */
  def partKit_=(partKit: PartKit) = {
    kitName = partKit.kitName.get
    kitDescription = partKit.description.get
    partKit.parts.get foreach (addLineItem(_))
  }
}
