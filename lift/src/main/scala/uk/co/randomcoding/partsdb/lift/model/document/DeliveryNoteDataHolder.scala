/**
 *
 */
package uk.co.randomcoding.partsdb.lift.model.document

import net.liftweb.util.Cell
import net.liftweb.util.ValueCell
import uk.co.randomcoding.partsdb.core.document.Document
import uk.co.randomcoding.partsdb.core.address.Address

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class DeliveryNoteDataHolder extends DocumentDataHolder with LineItemsDataHolder {
  /**
   * The total computed base cost of the line items, before tax
   */
  override val preTaxTotal: Cell[Double] = itemsPreTaxSubTotal.lift(carriageCell)(_ + _)

  override val lineItemsSubTotalCell: Cell[Double] = itemsPreTaxSubTotal.lift(_ + 0.0d)

  val selectedOrderCell = ValueCell[Option[Document]](None)

  val poReferenceCell = selectedOrderCell.lift(_ match {
    case Some(d) => d.customerPoReference.get
    case _ => ""
  })

  private val deliveryAddressCell = ValueCell[Option[Address]](None)

  // Accessor functions

  def selectedOrder = selectedOrderCell.get

  /**
   * Setter for the value of the selected order cell.
   *
   * This will also update the carriage value in the parent [[uk.co.randomcoding.partsdb.lift.model.document.DocumentDataHolder]]
   */
  def selectedOrder_=(order: Option[Document]) = {
    selectedOrderCell set order
    carriage = if (order isDefined) order.get.carriage.get else 0.0d
  }

  def deliveryAddress = deliveryAddressCell.get

  def deliveryAddress_=(addr: Option[Address]) = deliveryAddressCell set addr
}