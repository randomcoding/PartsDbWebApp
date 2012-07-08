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

/**
 *
 */
package uk.co.randomcoding.partsdb.lift.model.document

import net.liftweb.util.Cell
import net.liftweb.util.ValueCell
import uk.co.randomcoding.partsdb.core.document.Document
import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.document.LineItem
import uk.co.randomcoding.partsdb.core.customer.Customer

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class InvoiceDataHolder extends DocumentDataHolder with LineItemsDataHolder {

  /**
   * The total computed base cost of the line items, before tax
   */
  override val preTaxTotal: Cell[Double] = itemsPreTaxSubTotal.lift(carriageCell)(_ + _)

  override val lineItemsSubTotalCell: Cell[Double] = itemsPreTaxSubTotal.lift(_ + 0.0d)

  private[this] val invoicedDeliveryNotesCell = ValueCell[Seq[Document]](Nil)

  private[this] val invoiceAddressCell = ValueCell[Option[Address]](None)

  private[this] val deliveryNotesCell = ValueCell[Seq[Document]](Nil)

  // Accessor functions

  /**
   * Get the Delivery Address
   */
  def invoiceAddress = invoiceAddressCell.get

  /**
   * Set the delivery address cell value
   */
  def invoiceAddress_=(addr: Option[Address]) = invoiceAddressCell.set(addr)

  /**
   * Add a new delivery note to the current delivery notes.
   *
   * Also updates the carriage total and line items being invoiced
   */
  def addDeliveryNote(deliveryNote: Document): Unit = deliveryNotesCell.atomicUpdate(deliveryNotes => deliveryNotes find (_.id.get == deliveryNote.id.get) match {
    case Some(dn) => deliveryNotes
    case None => {
      deliveryNote.lineItems.get foreach (addLineItem)
      carriage = carriageValue + deliveryNote.carriage.get
      deliveryNotes :+ deliveryNote
    }
  })

  /**
   * Remove a delivery note from the current delivery notes.
   *
   * Also updates the carriage total and line items being invoiced
   */
  def removeDeliveryNote(deliveryNote: Document): Unit = {
    deliveryNotesCell atomicUpdate (_ filterNot (_ == deliveryNote))
    deliveryNote.lineItems.get foreach (removeLineItem)
    carriage = carriageValue - deliveryNote.carriage.get
  }

  /**
   * Get a comma separated list of the current delivery note `documentNumber`s
   */
  def deliveryNoteIds = deliveryNotesCell.lift(_ map (_ documentNumber) mkString ", ")

  /**
   * Get a list of the customer P/O numbers from the currently selected delivery notes as a comma separated list
   */
  def poRef = deliveryNotesCell.lift(_ map (_.customerPoReference.get) mkString ", ")

  /**
   * Get the currently selected delivery notes
   */
  def deliveryNotes = deliveryNotesCell.get
}
