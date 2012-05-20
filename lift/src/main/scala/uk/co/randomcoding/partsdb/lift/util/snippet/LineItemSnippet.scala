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

/**
 * A snippet to process the display an  addition/update of [[uk.co.randomcoding.partsdb.core.document.LineItem]]s
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait LineItemSnippet extends ErrorDisplay with AllLineItemsSnippet with Logger {
  val dataHolder: NewLineItemDataHolder

  val parts = Part orderAsc (_.partName) fetch
  val partsSelect = (None, "Select Part") :: (parts map ((p: Part) => (Some(p), p.partName.get)))

  /**
   * Render the controls to add or edit a Line Item plus the button to add it to the `dataHolder`.
   *
   * @param addButtonText The text to display on the '''Add''' button. Defaults to ''Add Line''
   * @param additionalAddLineAction The `JsCmd` to execute in addition to adding the line item to the `dataHolder`.
   * This is executed after the add operation and defaults to a `Noop`
   */
  def renderAddEditLineItem(addButtonText: String = "Add Line", additionalAddLineAction: JsCmd = Noop) = {
    "#addLineButton" #> styledAjaxButton(addButtonText, (() => (addLine & additionalAddLineAction))) &
      "#partName" #> partNameContent() & // these can be done with WiringUI.toNode
      "#supplierName" #> suppliersContent() &
      "#partQuantity" #> partQuantityContent() &
      "#basePartCost" #> WiringUI.asText(dataHolder.currentPartBaseCostDisplay) &
      "#markup" #> styledAjaxText(dataHolder.markup, updateAjaxValue(dataHolder.markup(_)))
  }

  private[this] def partNameContent() = styledAjaxObjectSelect[Option[Part]](partsSelect, dataHolder.currentPart, updateAjaxValue(dataHolder.currentPart(_), refreshSuppliers), List(("id" -> "partName")))

  private[this] def partQuantityContent() = styledAjaxText(dataHolder.quantity, updateAjaxValue(quantity => dataHolder.quantity(asInt(quantity) match {
    case Full(q) => q
    case _ => 0
  })), List(("id" -> "partQuantity")))

  private[this] val suppliersContent = () => styledAjaxObjectSelect[Option[Supplier]](dataHolder.suppliers, dataHolder.supplier, updateAjaxValue(dataHolder.supplier(_)), List(("id" -> "supplierName")))

  private[this] def refreshQuantity(): JsCmd = SHtml.ajaxInvoke(() => {
    val html = partQuantityContent()
    Replace("partQuantity", html)
  })._2.cmd

  /**
   * Update the suppliers combo box based on the currently selected part (as stored in the Quote Holder
   */
  private[this] def refreshSuppliers(): JsCmd = SHtml.ajaxInvoke(() => {
    val html = suppliersContent()
    debug("Setting suppliers html to: %s".format(html))
    Replace("supplierName", html)
  })._2.cmd

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

    refreshLineItemDisplay() & refreshPartName() & refreshSuppliers() & refreshQuantity()
  }
}