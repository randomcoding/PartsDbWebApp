/**
 *
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

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddEditDelivery extends StatefulValidatingErrorDisplaySnippet with TransactionSnippet with AvailableLineItemsDisplay with AllLineItemsSnippet with DocumentDataHolderTotalsDisplay with AddressSnippet with SubmitAndCancelSnippet {

  override val dataHolder = new DeliveryNoteDataHolder
  dataHolder.customer = customer

  override var addressText: String = ""
  override var addressCountry: String = ""

  private[this] lazy val previousDeliveryNotes = documentsOfType(DocumentType.DeliveryNote)
  private[this] lazy val orders = documentsOfType(DocumentType.Order).toList

  private[this] lazy val ordersSelection = (None, "Select Order") :: (orders map (order => (Some(order), textForOrder(order))))

  private[this] val itemsOrdered = orders flatMap (_.lineItems.get)

  private[this] var selectedLineItems: Seq[LineItem] = Seq.empty

  private[this] def availableAddresses = {
    val (customerTransactions, customerAddress) = customer match {
      case Some(cust) => (Transaction where (_.customer eqs cust.id.get) fetch, Address where (_.id eqs cust.businessAddress.get) get)
      case _ => (Nil, None)
    }

    val deliveryNotesForCustomer = customerTransactions flatMap (tr => Document where (_.id in tr.documents.get) and (_.documentType eqs DocumentType.DeliveryNote) fetch)

    val deliveredToAddresses = deliveryNotesForCustomer map (_.documentAddress.get) sortBy (_.shortName.get)

    if (customerAddress isDefined) (customerAddress.get :: deliveredToAddresses).distinct else deliveredToAddresses
  }

  private[this] lazy val addressSelection = (None, "Select Delivery Address") :: (availableAddresses map (addr => (Some(addr), addr.shortName.get)))

  private[this] var confirmCloseOrder = false

  override lazy val cameFrom = S.referer openOr (customer match {
    case Some(c) => "/app/display/customer?id=%s".format(c.id.get.toString)
    case _ => "/app/show?entityType=Customer"
  })

  override def dispatch = {
    case "render" => render
  }

  def render = {
    dataHolder.deliveredItems = previousDeliveryNotes flatMap (_.lineItems.get)

    "#formTitle" #> Text("Create Delivery Note") &
      renderTransactionDetails() &
      "#carriage" #> WiringUI.asText(dataHolder.carriage) &
      "#selectOrder" #> styledAjaxObjectSelect(ordersSelection, None, updateAjaxValue[Option[Document]](updateOrderValue(_), refreshLineItemEntries())) &
      "#customerPoRefEntry" #> WiringUI.asText(dataHolder.poReference) &
      "#addressSelect" #> styledAjaxObjectSelect(addressSelection, None, updateAjaxValue[Option[Address]](dataHolder.deliveryAddress = _)) &
      renderEditableAddress("Delivery Address", customer) &
      renderAvailableLineItems(dataHolder.availableLineItems) &
      renderAllLineItems() &
      renderDocumentTotals() &
      "#orderId" #> WiringUI.asText(dataHolder.orderId) &
      "#confirmCloseOrder" #> styledCheckbox(false, confirmCloseOrder = _) &
      renderSubmitAndCancel()
  }

  private[this] val itemsToBeDelivered = () => dataHolder.lineItems match {
    case Nil => Seq("Please Select at least one item to be delivered")
    case _ => Nil
  }

  private[this] lazy val confirmAddressSelectedOrEntered = () => if (deliverToAddress isDefined) Nil else Seq("Please Select or Enter a New Delivery Address")

  private[this] lazy val confirmOrderClose = () => if (confirmCloseOrder) Nil else Seq("Please confirm it is ok to close the Order before generating this Delivery Note")

  private[this] lazy val confirmAllOrderedItemsSelected = () => if (dataHolder.availableLineItems filterNot (item => dataHolder.deliveredItems.contains(item)) isEmpty) Nil else Seq("Please Select all Line Items to be added to the Delivery Note")

  private[this] lazy val additionalValidations = Seq(itemsToBeDelivered, confirmAddressSelectedOrEntered, confirmOrderClose, confirmAllOrderedItemsSelected)

  override def processSubmit(): JsCmd = performValidation(additionalValidations: _*) match {
    case Nil => generateDeliveryNote()
    case errors => {
      displayErrors(errors: _*)
      Noop
    }
  }

  private[this] def deliverToAddress = addressFromInput(addressName) match {
    case Some(addr) => Some(addr)
    case _ => dataHolder.deliveryAddress
  }

  private[this] def generateDeliveryNote() = {
    val deliveryNote = DeliveryNote.create(dataHolder.lineItems, dataHolder.carriageValue, dataHolder.poReference.get).documentAddress(deliverToAddress.get)

    Document.add(deliveryNote) match {
      case Some(dn) => {
        Transaction.addDocument(transaction.get.id.get, dn.id.get)
        Document.close(dataHolder.selectedOrder.get.id.get)
        S redirectTo "/app/display/customer?id=%s".format(transaction.get.customer.get.toString)
      }
      case _ => {
        displayError("Failed To create Delivery Note. Please send an Error Report")
        Noop
      }
    }
  }

  private[this] val updateOrderValue = (value: Option[Document]) => {
    dataHolder selectedOrder = value
    dataHolder.lineItemsCell.set(Nil)
  }

  private[this] def refreshLineItemEntries(): JsCmd = ajaxInvoke(() => refreshAvailableLineItems(dataHolder.availableLineItems) &
    refreshLineItemDisplay())._2.cmd

  override def validationItems = Seq(ValidationItem(dataHolder.selectedOrder, "Selected Order"))

  override def checkBoxSelected(selected: Boolean, line: LineItem): JsCmd = {
    selected match {
      case true => dataHolder.addLineItem(line)
      case false => dataHolder.removeLineItem(line)
    }
    debug("Selected Line Items: %s".format(dataHolder.lineItems.mkString("[", ", ", "]")))
    refreshLineItemDisplay()
  }

  private[this] def textForOrder(order: Document): String = "%s (%s)".format(order.documentNumber, dateString(order.createdOn.get))
}