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

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddQuote extends StatefulSnippet with DbAccessSnippet with ErrorDisplay with DataValidation with Logger {

  var customerName = ""
  var currentPart: Option[Part] = None
  var partId: Identifier = DefaultIdentifier
  var lineItems: List[LineItem] = Nil
  var newPartIdentifier: Identifier = DefaultIdentifier
  var newQuantity = ""

  val parts = getAll[Part]("partId") sortBy (_.partName)

  val partsSelect = (None, "Select Part") :: (parts map ((p: Part) => (Some(p), p.partName)))

  val customers = getAll[Customer]("customerId")
  val customersSelect = customers map ((c: Customer) => (Some(c), c.customerName))
  var currentCustomer: Option[Customer] = None

  def dispatch = {
    case "render" => render
  }

  def addLine(): JsCmd = {

    val quantity = asInt(newQuantity) match {
      case Full(q) => q
      case _ => -1
    }
    debug("CurrentPart %s".format(currentPart))
    // TODO: validation
    currentPart match {
      case Some(p) => {
        val line = LineItem(lineItems.size, newPartIdentifier, quantity, p.partCost)
        lineItems = line :: lineItems
      }
      case None => // do nothing
    }
    JsCmds.SetHtml("currentLineItems", DisplayLineItem.displayTable(lineItems))
  }

  def render = {
    "#formTitle" #> Text("Add Quote") &
      "#customerSelect" #> styledObjectSelect[Option[Customer]](customersSelect, None, currentCustomer = _) &
      "#addLineButton" #> styledAjaxButton("Add Line", addLine) &
      "#partName" #> styledObjectSelect[Option[Part]](partsSelect, currentPart, currentPart = _) &
      "#partQuantity" #> styledText(newQuantity, newQuantity = _) &
      "#submit" #> button("Save", processSubmit)
  }

  private[this] def processSubmit() = {
    addQuote(lineItems)
    S.redirectTo("/app/")
  }
}