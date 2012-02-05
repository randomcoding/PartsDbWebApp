/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import scala.xml.Text
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.part.Part
import uk.co.randomcoding.partsdb.lift.model.document.QuoteHolder
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet.{ ErrorDisplay, DataValidation }
import net.liftweb.common.StringOrNodeSeq.strTo
import net.liftweb.common.{ Logger, Full }
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmds.{ SetHtml, Noop }
import net.liftweb.http.js.JsCmd.unitToJsCmd
import net.liftweb.http.js.jquery.JqWiringSupport
import net.liftweb.http.js.JsCmd
import net.liftweb.http.{ WiringUI, StatefulSnippet, S }
import net.liftweb.util.Helpers._
import net.liftweb.util.ValueCell._
import uk.co.randomcoding.partsdb.core.supplier.Supplier

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddEditQuote extends StatefulSnippet with ErrorDisplay with DataValidation with Logger {

  var customerName = ""
  val quoteHolder = new QuoteHolder

  val parts = List.empty[Part] //getAll[Part]("partId") sortBy (_.partName)
  val partsSelect = (None, "Select Part") :: (parts map ((p: Part) => (Some(p), p.partName.get)))

  val customers = List.empty[Customer] //getAll[Customer]("customerId")
  val customersSelect = (None, "Select Customer") :: (customers map ((c: Customer) => (Some(c), c.customerName.get)))
  var currentCustomer: Option[Customer] = None

  def dispatch = {
    case "render" => render
  }

  def render = {
    "#formTitle" #> Text("Add Quote") &
      "#customerSelect" #> styledObjectSelect[Option[Customer]](customersSelect, None, currentCustomer = _) &
      "#addLineButton" #> styledAjaxButton("Add Line", addLine) &
      "#partName" #> styledAjaxObjectSelect[Option[Part]](partsSelect, quoteHolder.currentPart, updateHolderValue(part => {
        quoteHolder currentPart part
        // update the suppliers
      })) &
      "#supplierName" #> styledAjaxObjectSelect[Option[Supplier]](quoteHolder.suppliers, quoteHolder.supplier, updateHolderValue(quoteHolder.supplier(_))) &
      "#partQuantity" #> styledAjaxText(quoteHolder.quantity, updateHolderValue(quantity => quoteHolder.quantity(asInt(quantity) match {
        case Full(q) => q
        case _ => 0
      }))) &
      "#basePartCost" #> WiringUI.asText(quoteHolder.currentPartBaseCostDisplay) &
      "#markup" #> styledAjaxText(quoteHolder.markup, updateHolderValue(quoteHolder.markup(_))) &
      "#submit" #> button("Save Quote", processSubmit) &
      "#currentLineItems" #> DisplayLineItem.displayTable(quoteHolder.lineItems) &
      "#subTotal" #> WiringUI.asText(quoteHolder.subTotal) &
      "#vatAmount" #> WiringUI.asText(quoteHolder.vatAmount) &
      "#totalCost" #> WiringUI.asText(quoteHolder.totalCost, JqWiringSupport.fade)
  }

  /**
   * Function to provide ajax updates to the quote holder
   */
  private def updateHolderValue[T](updateFunc: (T) => Any, jscmd: JsCmd = Noop): T => JsCmd = {
    (t: T) =>
      {
        updateFunc(t)
        jscmd
      }
  }

  /**
   * Add a new line item to the quote holder and refresh the line items display.
   *
   *  If the line item part is already present, simply update the value and refresh.
   */
  //  private def addLine(): JsCmd = {
  //    clearErrors
  //    (asInt(quoteHolder.quantity), quoteHolder.currentPart) match {
  //      case (Full(q), Some(part)) => quoteHolder.addLineItem()
  //      case (Full(q), None) => displayError("partErrorId", "Please select a Part")
  //      case (_, Some(part)) => displayError("quantityErrorId", "Please specify a valid quantity")
  //      case (_, None) => {
  //        displayError("quantityErrorId", "Please specify a valid quantity")
  //        displayError("partErrorId", "Please select a Part")
  //      }
  //    }

  private def addLine(): JsCmd = {
    clearErrors
    (asInt(quoteHolder.quantity), quoteHolder.currentPart) match {
      case (Full(q), Some(part)) => quoteHolder.addLineItem()
      case (Full(q), None) => displayError("errorMessages", "Please select a Part")
      case (_, Some(part)) => displayError("errorMessages", "Please specify a valid quantity")
      case (_, None) => {
        displayError("errorMessages", "Please specify a valid quantity")
        displayError("errorMessages", "Please select a Part")
      }
    }

    refreshLineItemDisplay()
  }

  private def refreshLineItemDisplay(): JsCmd = {
    SetHtml("currentLineItems", DisplayLineItem.displayTable(quoteHolder.lineItems))
  }

  private[this] def processSubmit() = {
    currentCustomer match {
      case Some(cust) => {
        //addQuote(quoteHolder.lineItems, cust.customerId)
        S.redirectTo("/app/")
      }
      //case None => displayError("customerErrorId", "Please select a Customer")
      case None => displayError("errorMessages", "Please select a Customer")
    }
  }
}