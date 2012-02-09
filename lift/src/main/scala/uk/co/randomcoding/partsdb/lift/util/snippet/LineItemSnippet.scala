/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet

import uk.co.randomcoding.partsdb.lift.model.document.QuoteHolder
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import net.liftweb.common.Logger
import net.liftweb.util.Helpers._
import uk.co.randomcoding.partsdb.core.part.Part
import uk.co.randomcoding.partsdb.lift.util.LineItemDisplay
import com.foursquare.rogue.Rogue._
import uk.co.randomcoding.partsdb.core.supplier.Supplier
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds.Replace
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.common.Full
import net.liftweb.http.WiringUI

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait LineItemSnippet extends ErrorDisplay with Logger {

  val quoteHolder: QuoteHolder

  val parts = Part where (_.id exists true) orderDesc (_.partName) fetch
  val partsSelect = (None, "Select Part") :: (parts map ((p: Part) => (Some(p), p.partName.get)))

  def renderAddEditLineItem() = {
    "#addLineButton" #> styledAjaxButton("Add Line", addLine) &
      "#partName" #> styledAjaxObjectSelect[Option[Part]](partsSelect, quoteHolder.currentPart, updateAjaxValue(part => {
        quoteHolder currentPart part
        refreshSuppliers()
      })) &
      "#supplierName" #> suppliersContent() &
      "#partQuantity" #> styledAjaxText(quoteHolder.quantity, updateAjaxValue(quantity => quoteHolder.quantity(asInt(quantity) match {
        case Full(q) => q
        case _ => 0
      }))) &
      "#basePartCost" #> WiringUI.asText(quoteHolder.currentPartBaseCostDisplay) &
      "#markup" #> styledAjaxText(quoteHolder.markup, updateAjaxValue(quoteHolder.markup(_)))
  }

  def renderAllLineItems() = {
    "#currentLineItems" #> LineItemDisplay.displayTable(quoteHolder.lineItems)
  }

  private[this] val suppliersContent = () => styledAjaxObjectSelect[Option[Supplier]](quoteHolder.suppliers, quoteHolder.supplier, updateAjaxValue(quoteHolder.supplier(_)))

  private[this] def refreshSuppliers(): JsCmd = {
    val html = suppliersContent()
    debug("Setting suppliers html to: %s".format(html))
    Replace("supplierName", html)
  }

  /**
   * Add a new line item to the quote holder and refresh the line items display.
   *
   *  If the line item part is already present, simply update the value and refresh.
   */
  private def addLine(): JsCmd = {
    clearErrors
    (asInt(quoteHolder.quantity), quoteHolder.currentPart) match {
      case (Full(q), Some(part)) => quoteHolder.addLineItem()
      case (Full(q), None) => displayError("partErrorId", "Please select a Part")
      case (_, Some(part)) => displayError("quantityErrorId", "Please specify a valid quantity")
      case (_, None) => {
        displayError("quantityErrorId", "Please specify a valid quantity")
        displayError("partErrorId", "Please select a Part")
      }
    }

    refreshLineItemDisplay()
  }

  private def refreshLineItemDisplay(): JsCmd = {
    SetHtml("currentLineItems", LineItemDisplay.displayTable(quoteHolder.lineItems))
  }
}