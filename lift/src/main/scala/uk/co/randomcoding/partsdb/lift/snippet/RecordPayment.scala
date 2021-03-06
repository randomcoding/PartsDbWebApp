
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

import java.util.Date

import scala.xml.Text

import com.foursquare.rogue.Rogue._
import uk.co.randomcoding.partsdb.core.transaction.Payment
import uk.co.randomcoding.partsdb.lift.model.Session.recentNewPayments
import uk.co.randomcoding.partsdb.lift.util.DateHelpers._
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet._

import net.liftweb.common.{ Logger, Full }
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.http.js.JsCmd
import net.liftweb.http.{ StatefulSnippet, S }
import net.liftweb.util.Helpers._

/**
 * Records one or more invoicePayments to allow them to later be matched against invoices.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class RecordPayment extends StatefulSnippet with ErrorDisplay with DataValidation with Logger {

  override def dispatch = {
    case "render" => render
  }

  private[this] var paymentReference = ""
  private[this] var paymentValueText = ""
  private[this] var paymentDateText = ""

  def render = {
    "#paymentReferenceEntry" #> styledAjaxText(paymentReference, paymentReference = _) &
      "#paymentValueEntry" #> styledAjaxText(paymentValueText, paymentValueText = _) &
      "#paymentDateEntry" #> styledAjaxDatePicker("paymentDateEntry", paymentDateText, paymentDateText = _, datePickerAttrs = List("readonly" -> "true")) &
      "#cancel" #> styledAjaxButton("Cancel", () => S redirectTo "/app/") &
      "#submit" #> styledAjaxButton("Record Payment", recordAndClear) &
      "#payInvoices" #> styledAjaxButton("Pay Invoices", commitPaymentsAndGoToCustomerPage) &
      "#allRecordedPayments *" #> renderCurrentPayments
  }

  private[this] def addPaymentsToDatabase() = {
    for {
      payment <- recentNewPayments
      if Payment.add(payment) isEmpty
    } yield {
      "Failed to add payment %s to the database"
    }
  }

  private[this] def renderCurrentPayments = recentNewPayments map (payment => {
    "#payRef" #> Text(payment.paymentReference.get) &
      "#payDate" #> Text(dateString(payment.paymentDate.get)) &
      "#payValue" #> Text("£%.2f".format(payment.paymentAmount.get))
  })

  private[this] val recordAndClear: () => JsCmd = () => (recordPayment() & S.redirectTo("/app/recordPayment"))

  private[this] def paymentDate: Date = date(paymentDateText)

  private[this] val paymentNameIsUnique = () => Payment where (_.paymentReference eqs paymentReference) get match {
    case None => Nil
    case _ => Seq("There is already a Payment with a reference of %s".format(paymentReference))
  }

  private[this] val recordPayment: () => JsCmd = () => {
    debug("Payment Ref: %s".format(paymentReference))
    debug("Payment Value: %s".format(paymentValueText))
    debug("Payment Date: %s".format(paymentDateText))

    performValidation(paymentNameIsUnique) match {
      case Nil => {
        val payment = Payment.create(asDouble(paymentValueText).get, paymentReference, Nil, paymentDate)
        debug("Created Payment Object: %s".format(payment))
        recentNewPayments.atomicUpdate(recentNewPayments => recentNewPayments :+ payment)
        S redirectTo "/app/recordPayment"
      }
      case errors => {
        displayErrors(errors: _*)
        Noop
      }
    }
  }

  private[this] val commitPaymentsAndGoToCustomerPage: () => JsCmd = () => {
    addPaymentsToDatabase() match {
      case Nil => {
        recentNewPayments.set(Nil)
        S redirectTo "/app/payInvoices"
      }
      case errors => {
        displayErrors(errors: _*)
        Noop
      }
    }
  }

  override def validationItems(): Seq[ValidationItem] = {
    Seq(ValidationItem(paymentReference, "Payment Reference"),
      ValidationItem(paymentDateText, "Payment Date"),
      ValidationItem(asDouble(paymentValueText) match {
        case Full(d) => d
        case _ => -1.0d
      }, "Payment Value"))
  }

}
