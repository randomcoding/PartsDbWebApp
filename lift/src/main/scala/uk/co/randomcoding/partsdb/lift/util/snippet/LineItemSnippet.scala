/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet

import com.foursquare.rogue.Rogue._
import uk.co.randomcoding.partsdb.core.part.Part
import uk.co.randomcoding.partsdb.core.supplier.Supplier
import uk.co.randomcoding.partsdb.lift.model.document.QuoteHolder
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet._
import uk.co.randomcoding.partsdb.lift.util._
import net.liftweb.common.{ Logger, Full }
import net.liftweb.http.js.JsCmds.{ SetHtml, Replace }
import net.liftweb.http.js.JsCmd
import net.liftweb.http.{ WiringUI, SHtml }
import net.liftweb.util.Helpers._
import net.liftweb.http.SHtml.ElemAttr

/**
 * A snippet to process the display an update of [[uk.co.randomcoding.partsdb.core.document.LineItem]]s
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait LineItemSnippet extends ErrorDisplay with Logger {
  val quoteHolder: QuoteHolder

  val parts = Part where (_.id exists true) orderDesc (_.partName) fetch
  val partsSelect = (None, "Select Part") :: (parts map ((p: Part) => (Some(p), p.partName.get)))

  def renderAddEditLineItem() = {
    "#addLineButton" #> styledAjaxButton("Add Line", addLine) &
      "#partName" #> partNameContent() &
      "#supplierName" #> suppliersContent() &
      "#partQuantity" #> partQuantityContent() &
      "#basePartCost" #> WiringUI.asText(quoteHolder.currentPartBaseCostDisplay) &
      "#markup" #> styledAjaxText(quoteHolder.markup, updateAjaxValue(quoteHolder.markup(_)))
  }

  def renderAllLineItems() = "#currentLineItems" #> LineItemDisplay(quoteHolder.lineItems, false, false)

  private[this] def partNameContent() = styledAjaxObjectSelect[Option[Part]](partsSelect, quoteHolder.currentPart, updateAjaxValue(quoteHolder.currentPart(_), refreshSuppliers), List(("id" -> "partName")))

  private[this] def partQuantityContent() = styledAjaxText(quoteHolder.quantity, updateAjaxValue(quantity => quoteHolder.quantity(asInt(quantity) match {
    case Full(q) => q
    case _ => 0
  })), List(("id" -> "partQuantity")))

  private[this] val suppliersContent = () => styledAjaxObjectSelect[Option[Supplier]](quoteHolder.suppliers, quoteHolder.supplier, updateAjaxValue(quoteHolder.supplier(_)), List(("id" -> "supplierName")))

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
    (asInt(quoteHolder.quantity), quoteHolder.currentPart) match {
      case (Full(q), Some(part)) => {
        quoteHolder.addLineItem()
      }
      case (Full(q), None) => displayError("Please select a Part")
      case (_, Some(part)) => displayError("Please specify a valid quantity")
      case (_, None) => displayErrors("Please specify a valid quantity", "Please select a Part")
    }

    refreshLineItemDisplay() & refreshPartName() & refreshSuppliers() & refreshQuantity()
  }

  private def refreshLineItemDisplay(): JsCmd = SetHtml("currentLineItems", LineItemDisplay(quoteHolder.lineItems, false, false))
}