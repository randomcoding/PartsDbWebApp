/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import net.liftweb.util.Helpers._
import net.liftweb.http.SHtml.ElemAttr._
import net.liftweb.http.SHtml.button
import net.liftweb.http.S
import net.liftweb.http.js.JsCmds.Noop
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import net.liftweb.http.StatefulSnippet
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds.Replace

/**
 * Records one or more payments to allow them to later be matched against invoices.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class RecordPayment extends StatefulSnippet {

  override def dispatch = {
    case "render" => render
  }

  private[this] var paymentReference = ""
  private[this] var paymentValueText = ""
  private[this] var paymentDateText = ""

  def render = {
    "#paymentReferenceEntry" #> renderPaymentReference &
      "#paymentValueEntry" #> renderPaymentValue &
      "#paymentDateEntry" #> renderPaymentDate &
      "#cancel" #> styledAjaxButton("Cancel", () => S redirectTo "/app/") &
      "#submit" #> styledAjaxButton("Record Payment", recordPayment) &
      "#submitAndNew" #> styledAjaxButton("Record & New", recordAndClear)
  }

  private[this] def renderPaymentReference = styledText(paymentReference, paymentReference = _)

  private[this] def clearAndRefreshPaymentReference() = {
    paymentReference = ""
    Replace("paymentReferenceEntry", renderPaymentReference)
  }

  private[this] def renderPaymentValue = styledText(paymentValueText, paymentValueText = _)

  private[this] def clearAndRefreshPaymentValue() = {
    paymentValueText = ""
    Replace("paymentValueEntry", renderPaymentValue)
  }

  private[this] def renderPaymentDate = styledDatePicker("paymentDateEntry", paymentDateText, paymentDateText = _, datePickerAttrs = List("readonly" -> "true"))

  private[this] def clearAndRefreshPaymentDate() = {
    paymentDateText = ""
    Replace("paymentDateEntry", renderPaymentDate)
  }

  private[this] val recordAndClear: () => JsCmd = () => (recordPayment() & clearPaymentEntry())

  private[this] val recordPayment: () => JsCmd = () => {
    Noop
  }

  private[this] val clearPaymentEntry: () => JsCmd = () => {
    clearAndRefreshPaymentDate() &
      clearAndRefreshPaymentReference() &
      clearAndRefreshPaymentValue()
  }
}