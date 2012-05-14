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

import scala.xml.{ Text, NodeSeq }

import uk.co.randomcoding.partsdb.core.document.LineItem
import uk.co.randomcoding.partsdb.core.part.PartKit
import uk.co.randomcoding.partsdb.lift.model.PartKitDataHolder
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet._

import net.liftweb.common.Logger
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.http.js.JsCmd
import net.liftweb.http.{ WiringUI, StatefulSnippet, S }
import net.liftweb.util.Helpers._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddEditPartKit extends StatefulSnippet with Logger with SubmitAndCancelSnippet with LineItemSnippet with AllLineItemsSnippet {

  override val cameFrom = S.referer openOr "/app/"

  override val dataHolder = new PartKitDataHolder

  def dispatch = {
    case "render" => render
  }

  def render = {
    "#nameEntry" #> styledAjaxText(dataHolder.kitName, updateAjaxValue[String](name => dataHolder.kitName = name)) &
      "#descriptionEntry" #> styledAjaxTextArea(dataHolder.kitDescription, updateAjaxValue[String](name => dataHolder.kitDescription = name)) &
      renderAddEditLineItem("Add Item") &
      "#partKitContentsDisplay *" #> WiringUI.toNode(dataHolder.lineItemsCell)(renderPartKitLineItems) &
      renderSubmitAndCancel()
  }

  override def processSubmit(): JsCmd = Noop

  private[this] def renderPartKitLineItems: (List[LineItem], NodeSeq) => NodeSeq = (lines, nodes) => {
    lines flatMap (line => transformLineItem(line)(nodes))
  }

  private[this] val transformLineItem = (line: LineItem) => {
    "#itemName" #> (PartKit.findById(line.partId.get) match {
      case Some(pk) => Text(pk.kitName.get)
      case _ => Text("No Kit Name")
    }) &
      "#itemQuantity" #> Text("%.2f".format(line.quantity.get)) &
      "#itemCostPrice" #> ("£%.2f".format(line.lineCost)) &
      "#itemMarkup" #> "%d%%".format(line.markup.get)
  }
}