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
package uk.co.randomcoding.partsdb.lift.model.document

import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.document.{ LineItem, Document }

import net.liftweb.util.{ ValueCell, Cell }

/**
 * Data Holder for new `Delivery Note`s
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class DeliveryNoteDataHolder extends DocumentDataHolder with LineItemsDataHolder {

  /**
   * The total computed base cost of the line items, before tax
   */
  override val preTaxTotal: Cell[Double] = itemsPreTaxSubTotal.lift(carriageCell)(_ + _)

  override val lineItemsSubTotalCell: Cell[Double] = itemsPreTaxSubTotal.lift(_ + 0.0d)

  private[this] val deliveredItemsCell = ValueCell[Seq[LineItem]](Nil)

  private[this] val selectedOrderCell = ValueCell[Option[Document]](None)

  private[this] val poReferenceCell = selectedOrderCell.lift(_ match {
    case Some(d) => d.customerPoReference.get
    case _ => ""
  })

  private[this] val deliveryAddressCell = ValueCell[Option[Address]](None)

  private[this] val lineItemsFromOrder = selectedOrderCell.lift(_ match {
    case Some(o) => o.lineItems.get
    case _ => Nil
  })

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

  /**
   * Calculated value of the current order id (document number)
   */
  def orderId = selectedOrderCell.lift(_ match {
    case Some(o) => o.documentNumber
    case _ => ""
  })

  /**
   * Get the Delivery Address
   */
  def deliveryAddress = deliveryAddressCell.get

  /**
   * Set the delivery address cell value
   */
  def deliveryAddress_=(addr: Option[Address]) = deliveryAddressCell.set(addr)

  /**
   * Get the line items that are available to be delivered
   *
   * @return The line items from the current order that are not already present in the `deliveredItems`
   */
  def availableLineItems = lineItemsFromOrder.get filterNot (deliveredItems contains _)

  def availableLineItemsCell = lineItemsFromOrder.lift(_ filterNot (deliveredItems contains _))

  /**
   * Set the items that have already been delivered
   */
  def deliveredItems_=(items: Seq[LineItem]) = deliveredItemsCell.set(items)

  /**
   * Get the items that have already been delivered
   */
  def deliveredItems = deliveredItemsCell.get

  /**
   * Calculated value of the Customer's PO reference number
   */
  val poReference = poReferenceCell.lift("%s".format(_))
}
