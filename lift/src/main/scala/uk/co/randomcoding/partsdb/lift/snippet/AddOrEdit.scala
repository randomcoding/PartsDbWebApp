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
package uk.co.randomcoding.partsdb.lift.snippet

import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._

import net.liftweb.http.S
import net.liftweb.util.Helpers._

/**
 * Simple snippet that displays Add or Edit buttons.
 *
 * This snippet expects a parameter called ''entityType'' that determines the
 * text that is displayed and the link that is generated.
 *
 * It is also expecting `div` elements with two specific ids:
 *  - `add`
 *  - `edit`
 *
 * Which are transformed into the add and edit links respectively.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
@deprecated("I think this is no longer used", "0.7.0")
object AddOrEdit {

  /**
   * Transforms the `#add` and `#edit` divs into links with `add/edit?entityType=...` targets.
   *
   * @param entityType The type of entity that is to be added or edited. This value is used ''as is''
   * so should not contain spaces and any capitalisation is preserved.
   */
  def render = {
    val entityType = S.attr("entityType") openOr ("Unspecified")
    val addText = "Add " + entityType
    val editText = "View or Edit %s".format(entityType)

    "#add" #> buttonLink("add%s".format(entityType), addText) &
      "#edit" #> buttonLink("edit%s".format(entityType), editText)
  }

}
