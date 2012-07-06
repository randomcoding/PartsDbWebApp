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
import uk.co.randomcoding.partsdb.core.part.{ PartKit, Part }
import uk.co.randomcoding.partsdb.core.util.MongoHelpers._
import uk.co.randomcoding.partsdb.lift.model.PartKitDataHolder
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet._

import net.liftweb.common.{ Logger, Full }
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.http.js.JsCmd
import net.liftweb.http.{ WiringUI, StatefulSnippet, S }
import net.liftweb.util.Helpers._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddEditPartKit extends StatefulSnippet with Logger with SubmitAndCancelSnippet with LineItemSnippet with AllLineItemsSnippet with ErrorDisplay with DataValidation {

  override val cameFrom = () => S.referer openOr "/app/"

  override val dataHolder = new PartKitDataHolder

  override val selectPartKits = false

  private[this] val initialPartKit = S param "id" match {
    case Full(id) => getPartKitFromDatabaseAndUpdateDataHolder(id)
    case _ => None
  }

  def dispatch = {
    case "render" => render
  }

  def render = {
    "#formTitle" #> Text("Part Kit") &
      "#nameEntry" #> styledAjaxText(dataHolder.kitName, updateAjaxValue[String](name => dataHolder.kitName = name)) &
      "#descriptionEntry" #> styledAjaxTextArea(dataHolder.kitDescription, updateAjaxValue[String](name => dataHolder.kitDescription = name)) &
      renderAddEditLineItem("Add Item") &
      "#partKitItems" #> WiringUI.toNode(dataHolder.lineItemsCell)(renderPartKitLineItems) &
      renderSubmitAndCancel()
  }

  override def processSubmit(): JsCmd = performValidation() match {
    case Nil => initialPartKit match {
      case Some(partKit) => updatePartKit()
      case _ => addNewPartKit()
    }
    case errors => {
      displayErrors(errors: _*)
      Noop
    }
  }

  private[this] def responseForAddOrUpdate(func: () => Option[PartKit], addOrUpdate: String): JsCmd = {
    func() match {
      case Some(pk) => S redirectTo cameFrom()
      case None => {
        displayError("Failed To %s Part Kit. Please Submit an error report.".format(addOrUpdate))
        Noop
      }
    }
  }

  private[this] def addNewPartKit(): JsCmd = responseForAddOrUpdate(createPartKitAndAddToDatabase, "Add")

  private[this] def updatePartKit(): JsCmd = responseForAddOrUpdate(() => PartKit.update(initialPartKit.get.id.get, dataHolder.partKit), "Update")

  private[this] val createPartKitAndAddToDatabase: () => Option[PartKit] = () => initialPartKit match {
    case None => PartKit add dataHolder.partKit
    case Some(pk) => PartKit.update(pk.id.get, pk)
  }

  override def validationItems: Seq[ValidationItem] = Seq(ValidationItem(dataHolder.kitDescription, "Kit Description"),
    ValidationItem(dataHolder.kitName, "Kit Name"),
    ValidationItem(dataHolder.lineItems, "Kit Contents"))

  private[this] def renderPartKitLineItems: (List[LineItem], NodeSeq) => NodeSeq = (lines, nodes) => {
    debug("Received nodes for line items: %s".format(nodes.toString))
    val outNodes = lines flatMap (line => <tr>{ transformLineItem(line)(nodes) }</tr>)
    debug("Generated Nodes: %s".format(outNodes))
    outNodes
  }

  private[this] val transformLineItem = (line: LineItem) => {
    "#itemName" #> Text(lineItemPartName(line)) &
      "#itemQuantity" #> Text("%d".format(line.quantity.get)) &
      "#itemCostPrice" #> Text("£%.2f".format(line.costPrice)) &
      "#itemMarkup" #> Text("%.0f%%".format(line.markup.get * 100)) &
      "#itemFullPrice" #> Text("£%.2f".format(line.lineCost))
  }

  private[this] def lineItemPartName(line: LineItem): String = Part.findById(line.partId.get) match {
    case Some(part) => part.partName.get
    case _ => "No Part Name"
  }

  private[this] def getPartKitFromDatabaseAndUpdateDataHolder(id: String): Option[PartKit] = {
    PartKit.findById(id) match {
      case Some(pk) => {
        dataHolder.partKit = pk
        Some(pk)
      }
      case _ => None
    }
  }
}