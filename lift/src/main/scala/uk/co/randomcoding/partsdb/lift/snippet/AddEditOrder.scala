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

import scala.xml.Text
import org.bson.types.ObjectId
import com.foursquare.rogue.Rogue._
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.document.{ Order, LineItem, DocumentType, Document }
import uk.co.randomcoding.partsdb.core.part.Part
import uk.co.randomcoding.partsdb.core.transaction.Transaction
import uk.co.randomcoding.partsdb.lift.model.document.OrderDocumentDataHolder
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet.display.DocumentDataHolderTotalsDisplay
import uk.co.randomcoding.partsdb.lift.util.snippet._
import net.liftweb.common.Full
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.http.js.JsCmd
import net.liftweb.http.{ StatefulSnippet, S }
import net.liftweb.util.Helpers._
import uk.co.randomcoding.partsdb.core.address.Address
import net.liftweb.util.ValueCell

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddEditOrder extends StatefulValidatingErrorDisplaySnippet with TransactionSnippet with DocumentDataHolderTotalsDisplay with SubmitAndCancelSnippet with AllLineItemsSnippet with AvailableLineItemsDisplay {

  private[this] val defaultCameFrom = S.referer openOr "/app"

  override val dataHolder = new OrderDocumentDataHolder
  dataHolder.customer = customer

  override val cameFrom = () => dataHolder.customer match {
    case Some(cust) => "/app/display/customer?id=%s".format(cust.id.get)
    case _ => defaultCameFrom
  }

  private var customerPoRef = ""
  private var confirmCloseQuote = false

  // Each transaction should only have a single Quote
  private[this] val quote = documentsOfType(DocumentType.Quote) headOption
  private[this] val orders = documentsOfType(DocumentType.Order)

  private[this] val (carriage, lineItems, quoteId) = quote match {
    case Some(q) => {
      val orderedItems = orders flatMap (_.lineItems.get)
      dataHolder.carriage = q.carriage.get
      (q.carriage.get, q.lineItems.get filterNot (orderedItems contains _) sortBy (_.lineNumber.get), q.documentNumber)
    }
    case _ => (0.0d, List.empty, "No Quote")
  }

  private[this] val validateQuoteCloseConfirmation = () => if (confirmCloseQuote) Nil else Seq("Please confirm it is ok to close the Quote before generating this Order")

  private[this] val transactionHasValidAddress = () => if (customersAddress isDefined) Nil else (Seq("The parent transaction does not contain a valid customer address"))

  private[this] def customersAddress = Customer.findById(transaction.get.customer.get) match {
    case Some(cust) => Address.findById(cust.businessAddress.get)
    case _ => None
  }

  override def processSubmit(): JsCmd = performValidation(validateQuoteCloseConfirmation) match {
    case Nil => generateOrder()
    case errors => {
      displayErrors(errors: _*)
      Noop
    }
  }

  private[this] def generateOrder(): JsCmd = {
    val order = Order.create(dataHolder.lineItems, dataHolder.carriageValue, customerPoRef).documentAddress(customersAddress.get)

    Document.add(order) match {
      case Some(o) => {
        Transaction.addDocument(transaction.get.id.get, o.id.get)
        Document.close(quote.get.id.get)
        S redirectTo "/app/display/customer?id=%s".format(transaction.get.customer.get.toString)
      }
      case _ => {
        displayError("Failed to create Order. Please send an error report.")
        Noop
      }
    }
  }

  override def dispatch = {
    case "render" => render
  }

  def render = {
    "#formTitle" #> Text("Create Order") &
      "#transactionName" #> Text(transactionName) &
      "#customerName" #> Text(customerName) &
      renderDocumentTotals() &
      "#customerPoRefEntry" #> styledText(customerPoRef, customerPoRef = _) &
      renderAvailableLineItems(ValueCell(lineItems)) &
      renderAllLineItems() &
      "#quoteId" #> Text(quoteId) &
      "#confirmCloseQuote" #> styledCheckbox(false, confirmCloseQuote = _) &
      renderSubmitAndCancel()
  }

  override def validationItems(): Seq[ValidationItem] = Seq(ValidationItem(customerPoRef, "Customer P/O Reference"),
    ValidationItem(dataHolder.lineItems, "Selected Line Items"))

  override def checkBoxSelected(selected: Boolean, line: LineItem) = {
    selected match {
      case true => {
        if (dataHolder.lineItems isEmpty) dataHolder.carriage = carriage
        dataHolder.addLineItem(line)
      }
      case false => {
        dataHolder.removeLineItem(line)
        if (dataHolder.lineItems isEmpty) dataHolder.carriage = 0
      }
    }
    refreshLineItemDisplay()
  }
}
