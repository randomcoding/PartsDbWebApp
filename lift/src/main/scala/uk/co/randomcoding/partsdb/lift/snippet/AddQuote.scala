/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import scala.xml.Text

import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.part.Part
import uk.co.randomcoding.partsdb.lift.model.document.QuoteHolder
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers.{ styledObjectSelect, styledAjaxText, styledAjaxObjectSelect, styledAjaxButton }
import uk.co.randomcoding.partsdb.lift.util.snippet.{ ErrorDisplay, DbAccessSnippet, DataValidation }

import net.liftweb.common.StringOrNodeSeq.strTo
import net.liftweb.common.{ Logger, Full }
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmds.{ SetHtml, Noop }
import net.liftweb.http.js.JsCmd.unitToJsCmd
import net.liftweb.http.js.jquery.JqWiringSupport
import net.liftweb.http.js.JsCmd
import net.liftweb.http.{ WiringUI, StatefulSnippet, S }
import net.liftweb.util.Helpers._
import net.liftweb.util.ValueCell.vcToT

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddQuote extends StatefulSnippet with DbAccessSnippet with ErrorDisplay with DataValidation with Logger {

  var customerName = ""
  val quoteHolder = new QuoteHolder()

  var newQuantity = ""

  val parts = getAll[Part]("partId") sortBy (_.partName)
  val partsSelect = (None, "Select Part") :: (parts map ((p: Part) => (Some(p), p.partName)))

  val customers = getAll[Customer]("customerId")
  val customersSelect = customers map ((c: Customer) => (Some(c), c.customerName))
  var currentCustomer: Option[Customer] = None

  def dispatch = {
    case "render" => render
  }

  def render = {
    "#formTitle" #> Text("Add Quote") &
      "#customerSelect" #> styledObjectSelect[Option[Customer]](customersSelect, None, currentCustomer = _) &
      "#addLineButton" #> styledAjaxButton("Add Line", addLine) &
      "#partName" #> styledAjaxObjectSelect[Option[Part]](partsSelect, quoteHolder.currentPart, updateHolderValue(quoteHolder.currentPart(_))) &
      "#partQuantity" #> styledAjaxText(newQuantity, newQuantity = _) &
      "#basePartCost" #> WiringUI.asText(quoteHolder.currentPartBaseCostDisplay) &
      "#manualPartCost" #> styledAjaxText(quoteHolder.manualCost, updateHolderValue(quoteHolder.manualCost(_))) &
      "#submit" #> button("Save Quote", processSubmit) &
      "#currentLineItems" #> DisplayLineItem.displayTable(quoteHolder.quoteItems) &
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
  private def addLine(): JsCmd = {
    clearErrors
    (asInt(newQuantity), quoteHolder.currentPart) match {
      case (Full(q), Some(part)) => quoteHolder.setPartQuantity(part, q)
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
    SetHtml("currentLineItems", DisplayLineItem.displayTable(quoteHolder.quoteItems))
  }

  private[this] def processSubmit() = {
    currentCustomer match {
      case Some(cust) => {
        addQuote(quoteHolder.lineItems, cust.customerId)
        S.redirectTo("/app/")
      }
      case None => displayError("customerErrorId", "Please select a Customer")
    }
  }
}