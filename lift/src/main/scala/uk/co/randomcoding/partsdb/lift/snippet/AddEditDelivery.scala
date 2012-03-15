/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import uk.co.randomcoding.partsdb.lift.util.snippet.StatefulValidatingErrorDisplaySnippet
import net.liftweb.http.S
import net.liftweb.util.Helpers._
import net.liftweb.common.Full
import uk.co.randomcoding.partsdb.core.document.Document
import org.bson.types.ObjectId
import uk.co.randomcoding.partsdb.lift.util.snippet._
import uk.co.randomcoding.partsdb.lift.util.DateHelpers._
import scala.xml.Text
import uk.co.randomcoding.partsdb.core.document.DocumentType
import uk.co.randomcoding.partsdb.lift.snippet.js.JsScripts
import uk.co.randomcoding.partsdb.lift.util.snippet.display.DocumentDataHolderTotalsDisplay
import uk.co.randomcoding.partsdb.lift.model.document.DeliveryNoteDataHolder
import uk.co.randomcoding.partsdb.core.document.LineItem
import net.liftweb.http.WiringUI
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.core.transaction.Transaction
import com.foursquare.rogue.Rogue._
import uk.co.randomcoding.partsdb.core.address.Address

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddEditDelivery extends StatefulValidatingErrorDisplaySnippet with TransactionSnippet with AvailableLineItemsDisplay with AllLineItemsSnippet with DocumentDataHolderTotalsDisplay with AddressSnippet {

  override val dataHolder = new DeliveryNoteDataHolder

  override var addressText: String = ""
  override var addressCountry: String = ""

  private[this] lazy val previousDeliveryNotes = documentsOfType(DocumentType.DeliveryNote)
  private[this] lazy val orders = documentsOfType(DocumentType.Order).toList

  private[this] lazy val ordersSelection = (None, "Select Order") :: (orders map (order => (Some(order), textForOrder(order))))

  private[this] val itemsOrdered = orders flatMap (_.lineItems.get)
  private[this] val alreadyDeliveredItems = previousDeliveryNotes flatMap (_.lineItems.get)
  private[this] lazy val availableLineItems = itemsOrdered filterNot (alreadyDeliveredItems contains _)

  private[this] var selectedLineItems: Seq[LineItem] = Seq.empty

  private[this] def availableAddresses = {
    val (customerTransactions, customerAddress) = customer match {
      case Some(cust) => (Transaction where (_.customer eqs cust.id.get) fetch, Address where (_.id eqs cust.businessAddress.get) get)
      case _ => (Nil, None)
    }

    val deliveryNotesForCustomer = customerTransactions flatMap (tr => Document where (_.id in tr.documents.get) and (_.documentType eqs DocumentType.DeliveryNote) fetch)

    val deliveredToAddresses = deliveryNotesForCustomer map (_.deliveryAddress.get) sortBy (_.shortName.get)

    if (customerAddress isDefined) customerAddress.get :: deliveredToAddresses else deliveredToAddresses
  }

  private[this] lazy val addressSelection = (None, "Select Delivery Address") :: (availableAddresses map (addr => (Some(addr), addr.shortName.get)))

  override def dispatch = {
    case "render" => render
  }

  def render = {
    "#title" #> Text("Create Delivery Note") &
      "#scripts" #> JsScripts.accordionScript &
      renderTransactionDetails() &
      renderAvailableLineItems(availableLineItems) &
      renderDocumentTotals() &
      "#selectOrder" #> styledAjaxObjectSelect(ordersSelection, None, updateAjaxValue((value: Option[Document]) => dataHolder selectedOrder = value)) &
      "#customerPoRefEntry" #> WiringUI.asText(dataHolder.poReferenceCell) &
      "#carriage" #> WiringUI.asText(dataHolder.carriage) &
      "#addressSelect" #> styledAjaxObjectSelect(addressSelection, None, updateAjaxValue((value: Option[Address]) => dataHolder deliveryAddress = value)) &
      renderEditableAddress()
  }

  override val validationItems = Nil

  override def checkBoxSelected(selected: Boolean, line: LineItem) = {
    selected match {
      case true => dataHolder.addLineItem(line)
      case false => dataHolder.removeLineItem(line)
    }
    refreshLineItemDisplay()
  }

  private[this] def textForOrder(order: Document): String = "%s (%s)".format(order.documentNumber, dateString(order.createdOn.get))
}