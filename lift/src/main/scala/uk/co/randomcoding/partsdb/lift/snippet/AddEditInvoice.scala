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
import com.foursquare.rogue.Rogue._
import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.document.{ LineItem, DocumentType, Document, DeliveryNote }
import uk.co.randomcoding.partsdb.core.transaction.Transaction
import uk.co.randomcoding.partsdb.lift.model.document.DeliveryNoteDataHolder
import uk.co.randomcoding.partsdb.lift.util.DateHelpers._
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet.display.DocumentDataHolderTotalsDisplay
import uk.co.randomcoding.partsdb.lift.util.snippet._
import net.liftweb.common.Full
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.http.js.JsCmd
import net.liftweb.http.{ WiringUI, S }
import net.liftweb.util.Helpers._
import uk.co.randomcoding.partsdb.lift.model.document.InvoiceDataHolder
import uk.co.randomcoding.partsdb.lift.util.SnippetDisplayHelpers._
import uk.co.randomcoding.partsdb.core.document.Invoice
import uk.co.randomcoding.partsdb.core.system.SystemData

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddEditInvoice extends StatefulValidatingErrorDisplaySnippet with TransactionSnippet with AllLineItemsSnippet with DocumentDataHolderTotalsDisplay with AddressSnippet with SubmitAndCancelSnippet {

  override val dataHolder = new InvoiceDataHolder

  dataHolder.customer = customer

  override var addressText = ""
  override var addressCountry = ""

  private[this] lazy val previousInvoices = documentsOfType(DocumentType.Invoice)
  private[this] lazy val deliveries = documentsOfType(DocumentType.DeliveryNote).toList

  private[this] lazy val deliveryNoteSelection = (None, "Select Order") :: (deliveries map (deliveryNote => (Some(deliveryNote), textForDeliveryNote(deliveryNote))))

  private[this] var selectedLineItems: Seq[LineItem] = Seq.empty

  private[this] def availableAddresses = {
    val (customerTransactions, customerAddress) = customer match {
      case Some(cust) => (Transaction where (_.customer eqs cust.id.get) fetch, Address where (_.id eqs cust.businessAddress.get) get)
      case _ => (Nil, None)
    }

    val invoiceToAddresses = customerDocumentsOfType(customerTransactions, DocumentType.Invoice) map (_.documentAddress.get) sortBy (_.shortName.get)

    val deliveredToAddresses = customerDocumentsOfType(customerTransactions, DocumentType.DeliveryNote) map (_.documentAddress.get) sortBy (_.shortName.get)

    val storedAddresses = (invoiceToAddresses ++ deliveredToAddresses).distinct toList

    customerAddress match {
      case Some(addr) => addr :: (storedAddresses filterNot (_ == addr))
      case _ => storedAddresses
    }
  }

  private[this] def customerDocumentsOfType(customerTransactions: Seq[Transaction], documentType: DocumentType.DocType) = {
    customerTransactions flatMap (tr => Document where (_.id in tr.documents.get) and (_.documentType eqs documentType) fetch)
  }

  private[this] lazy val addressSelection = (None, "Select Invoice Address") :: (availableAddresses.toList map (addr => (Some(addr), addr.shortName.get)))

  private[this] var confirmCloseDeliveryNotes = false

  override val cameFrom = () => customer match {
    case Some(c) => "/app/display/customer?id=%s".format(c.id.get.toString)
    case _ => "/app/show?entityType=Customer"
  }

  override def dispatch = {
    case "render" => render
  }

  def render = {
    "#formTitle" #> Text("Raise Invoice") &
      renderTransactionDetails() &
      "#addressSelect" #> styledAjaxObjectSelect(addressSelection, None, updateAjaxValue[Option[Address]](dataHolder.invoiceAddress = _)) &
      "#availableDeliveryNotes *" #> renderAvailableDeliveryNotes(documentsOfType(DocumentType.DeliveryNote)) &
      renderEditableAddress("Invoice Address", customer) &
      renderAllLineItems() &
      renderDocumentTotals() &
      "#confirmCloseDeliveryNotes" #> styledCheckbox(false, confirmCloseDeliveryNotes = _) &
      "#deliveryNoteIds" #> WiringUI.asText(dataHolder.deliveryNoteIds) &
      renderSubmitAndCancel()
  }

  private[this] def renderAvailableDeliveryNotes(deliveryNotes: Seq[Document]) = {
    deliveryNotes map (deliveryNote => {
      "#selected" #> styledAjaxCheckbox(false, checkBoxSelected(_, deliveryNote)) &
        "#deliveryNoteId" #> Text(deliveryNote.documentNumber) &
        "#raisedOn" #> Text(dateString(deliveryNote.createdOn.get)) &
        "#totalDeliveryValue" #> Text(currencyFormat(deliveryNote.documentValue))
    })
  }

  private[this] val checkThereAreItemsToBeInvoiced = () => dataHolder.lineItems match {
    case Nil => Seq("Please Select at least one Delivery Note to be Invoiced")
    case _ => Nil
  }

  private[this] val checkConfirmCloseDeliveryNotes = () => confirmCloseDeliveryNotes match {
    case true => Nil
    case false => Seq("Please confirm it is ok to close the Order before generating this Delivery Note")
  }

  override def processSubmit(): JsCmd = performValidation(checkThereAreItemsToBeInvoiced, confirmAddressSelectedOrEntered, checkConfirmCloseDeliveryNotes) match {
    case Nil => generateInvoice()
    case errors => {
      displayErrors(errors: _*)
      Noop
    }
  }

  private[this] val confirmAddressSelectedOrEntered = () => if (invoiceToAddress isDefined) Nil else Seq("Please Select or Enter a New Invoice Address")

  private[this] def invoiceToAddress = addressFromInput(addressName) match {
    case Some(addr) => Some(addr)
    case _ => dataHolder.invoiceAddress
  }

  private[this] def generateInvoice() = {

    val invoice = Invoice.create(dataHolder.lineItems, dataHolder.carriageValue, invoicedDeliveryNotes = dataHolder.deliveryNotes).documentAddress(invoiceToAddress)

    Document.add(invoice) match {
      case Some(inv) => {
        Transaction.addDocument(transaction.get.id.get, inv.id.get)
        dataHolder.deliveryNotes foreach (Document close _.id.get)
        S redirectTo "/app/display/customer?id=%s".format(transaction.get.customer.get.toString)
      }
      case _ => {
        displayError("Failed To create Invoice. Please send an Error Report")
        Noop
      }
    }
  }

  override def validationItems = Nil

  private[this] def checkBoxSelected(selected: Boolean, deliveryNote: Document): JsCmd = {
    selected match {
      case true => dataHolder addDeliveryNote deliveryNote
      case false => dataHolder removeDeliveryNote deliveryNote
    }
    debug("Selected Line Items: %s".format(dataHolder.lineItems.mkString("[", ", ", "]")))
    refreshLineItemDisplay()
  }

  private[this] def textForDeliveryNote(deliveryNote: Document): String = "%s (%s)".format(deliveryNote.documentNumber, dateString(deliveryNote.createdOn.get))
}
