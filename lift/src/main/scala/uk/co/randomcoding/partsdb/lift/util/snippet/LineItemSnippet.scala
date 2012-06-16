/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet

import com.foursquare.rogue.Rogue._
import uk.co.randomcoding.partsdb.core.part.Part
import uk.co.randomcoding.partsdb.core.supplier.Supplier
import uk.co.randomcoding.partsdb.lift.model.document.NewLineItemDataHolder
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet._
import net.liftweb.common.{ Logger, Full }
import net.liftweb.http.js.JsCmds.{ Replace, Noop }
import net.liftweb.http.js.JsCmd
import net.liftweb.http.{ WiringUI, SHtml }
import net.liftweb.util.Helpers._
import uk.co.randomcoding.partsdb.core.part.PartKit
import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.field.ObjectIdPk
import scala.xml.NodeSeq

/**
 * A snippet to process the display an  addition/update of [[uk.co.randomcoding.partsdb.core.document.LineItem]]s
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait LineItemSnippet extends ErrorDisplay with AllLineItemsSnippet with Logger {
  val dataHolder: NewLineItemDataHolder

  /**
   * A flag used to set whether or not to allow the selection of PartKits in the part drop down.
   *
   * Defaults to `true` so Parts and PartKits are displayed.
   *
   * Override and set to false to only list parts
   */
  val selectPartKits: Boolean = true

  private[this] val parts = Part orderAsc (_.partName) fetch
  private[this] val partsSelect = parts map (p => (Some(p), p.partName.get))

  private[this] val partKits = PartKit orderAsc (_.kitName) fetch
  private[this] val partKitsSelect = partKits map (pk => (Some(pk), pk.kitName.get))

  private[this] val lineItemPartsSelect: List[(Option[MongoRecord[_] with ObjectIdPk[_]], String)] = selectPartKits match {
    case true => {
      val partsList = (None, "--- Parts ---") :: partsSelect
      val partKitsList = (None, "--- Part Kits ---") :: partKitsSelect

      (None, "Select Part") :: partsList ::: partKitsList
    }
    case false => (None, "Select Part") :: partsSelect
  }

  /**
   * Render the controls to add or edit a Line Item plus the button to add it to the `dataHolder`.
   *
   * @param addButtonText The text to display on the '''Add''' button. Defaults to ''Add Line''
   * @param additionalAddLineAction The `JsCmd` to execute in addition to adding the line item to the `dataHolder`.
   * This is executed after the add operation and defaults to a `Noop`
   */
  def renderAddEditLineItem(addButtonText: String = "Add/Update Line", additionalAddLineAction: JsCmd = Noop) = {
    "#addLineButton" #> styledAjaxButton(addButtonText, (() => (addLine & additionalAddLineAction))) &
      "#partName" #> partNameContent() & // these can be done with WiringUI.toNode
      "#supplierName" #> WiringUI.toNode(dataHolder.suppliersForPart)(renderSupplierChoice) &
      "#partQuantity" #> WiringUI.toNode(dataHolder.quantityCell)(renderPartQuantity) &
      "#basePartCost" #> WiringUI.asText(dataHolder.currentPartBaseCostDisplay) &
      "#markup" #> WiringUI.toNode(dataHolder.markupCell)(renderEditableMarkup)
  }

  private[this] val renderSupplierChoice: (List[(Option[Supplier], String)], NodeSeq) => NodeSeq = (s, nodes) => {
    styledAjaxObjectSelect[Option[Supplier]](s, dataHolder.supplier, updateAjaxValue(dataHolder.supplier(_)))
  }

  private[this] val renderPartQuantity: (Int, NodeSeq) => NodeSeq = (q, nodes) => {
    styledAjaxText("%d".format(q), updateAjaxValue(quantity => dataHolder.quantity(asInt(quantity) match {
      case Full(q) => q
      case _ => 0
    })), List(("id" -> "partQuantity")))
  }

  private[this] val renderEditableMarkup: (Int, NodeSeq) => NodeSeq = (inputMarkup, nodes) => {
    styledAjaxText("%d".format(inputMarkup), updateAjaxValue(dataHolder.markup(_)))
  }

  private[this] def partNameContent() = styledAjaxObjectSelect[Option[MongoRecord[_] with ObjectIdPk[_]]](lineItemPartsSelect, dataHolder.currentPart,
    updateAjaxValue(dataHolder.currentPart = _), List(("id" -> "partName")))

  /**
   * Update the part name combo based on the value in the Quote Holder
   */
  private[this] def refreshPartName(): JsCmd = SHtml.ajaxInvoke(() => {
    val html = partNameContent()
    Replace("partName", html)
  })._2.cmd

  /**
   * Add a new line item to the quote holder and refresh the line items display.
   *
   *  If the line item part is already present, simply update the value and refresh.
   */
  private def addLine(): JsCmd = {
    clearErrors
    (asInt(dataHolder.quantity), dataHolder.currentPart) match {
      case (Full(q), Some(part)) => {
        dataHolder.addLineItem()
      }
      case (Full(q), None) => displayError("Please select a Part")
      case (_, Some(part)) => displayError("Please specify a valid quantity")
      case (_, None) => displayErrors("Please specify a valid quantity", "Please select a Part")
    }

    refreshLineItemDisplay() & refreshPartName()
  }
}