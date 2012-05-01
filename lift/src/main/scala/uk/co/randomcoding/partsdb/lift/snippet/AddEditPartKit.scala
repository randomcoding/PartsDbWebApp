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
package uk.co.randomcoding.partsdb.lift.snippet

import scala.xml.Text

import uk.co.randomcoding.partsdb.lift.model.PartKitDataHolder
import uk.co.randomcoding.partsdb.lift.util.snippet._
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._

import net.liftweb.common.Logger
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.http.js.JsCmd
import net.liftweb.http.{ StatefulSnippet, S }
import net.liftweb.util.Helpers._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddEditPartKit extends StatefulSnippet with Logger with SubmitAndCancelSnippet with LineItemSnippet {

  override val cameFrom = S.referer openOr "/app/"

  override val dataHolder = new PartKitDataHolder

  def dispatch = {
    case "render" => render
  }

  def render = {
    "#nameEntry" #> styledAjaxText(dataHolder.kitName, updateAjaxValue[String](name => dataHolder.kitName = name)) &
      "#descriptionENtry" #> styledAjaxTextArea(dataHolder.kitDescription, updateAjaxValue[String](name => dataHolder.kitDescription = name)) &
      renderAddEditLineItem("Add Item") &
      "#thing" #> Text("stuff")
  }

  override def processSubmit(): JsCmd = Noop
}