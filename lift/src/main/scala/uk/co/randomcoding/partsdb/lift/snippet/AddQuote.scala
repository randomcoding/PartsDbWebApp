/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import scala.xml.Text
import uk.co.randomcoding.partsdb.core.document.LineItem
import uk.co.randomcoding.partsdb.core.id.{ DefaultIdentifier, Identifier }
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet.{ ErrorDisplay, DbAccessSnippet, DataValidation }
import net.liftweb.common.StringOrNodeSeq.strTo
import net.liftweb.common.Logger
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmds.{ Noop, SetHtml }
import net.liftweb.http.js.JsCmd
import net.liftweb.http.StatefulSnippet
import net.liftweb.util.Helpers._
import uk.co.randomcoding.partsdb.core.part.Part
import net.liftweb.http.js.JsCmds
import net.liftweb.common.Full
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.document.Document
import net.liftweb.http.S
import uk.co.randomcoding.partsdb.lift.model.document.QuoteHolder
import net.liftweb.common.Empty
import net.liftweb.http.WiringUI
import net.liftweb.http.js.jquery.JqWiringSupport

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddQuote extends StatefulSnippet with DbAccessSnippet with ErrorDisplay with DataValidation with Logger {

  var customerName = ""
  val quoteHolder = new QuoteHolder()

  var newQuantity = ""

  val parts = getAll[Part]("partId") sortBy (_.partName)
  val partsSelect = (None, "Select Part") :: (parts map ((p: Part) => (Some(p), p.partName)))
  var currentPart: Option[Part] = None

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
      "#partName" #> styledAjaxObjectSelect[Option[Part]](partsSelect, currentPart, currentPart = _) &
      "#partQuantity" #> styledAjaxText(newQuantity, newQuantity = _) &
      "#submit" #> button("Save Quote", processSubmit) &
      "#currentLineItems" #> DisplayLineItem.displayTable(quoteHolder.quoteItems) &
      "#subTotal" #> WiringUI.asText(quoteHolder.subTotal) &
      "#vatAmount" #> WiringUI.asText(quoteHolder.vatAmount) &
      "#totalCost" #> WiringUI.asText(quoteHolder.totalCost, JqWiringSupport.fade)
  }

  private def addLine(): JsCmd = {
    clearErrors
    (asInt(newQuantity), currentPart) match {
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
    addQuote(quoteHolder.buildQuote)
    S.redirectTo("/app/")
  }
}