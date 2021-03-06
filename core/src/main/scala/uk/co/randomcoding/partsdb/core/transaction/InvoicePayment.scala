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
package uk.co.randomcoding.partsdb.core.transaction

import net.liftweb.mongodb.record.BsonRecord
import net.liftweb.mongodb.record.field.ObjectIdRefField
import uk.co.randomcoding.partsdb.core.document.Document
import net.liftweb.record.field.DoubleField
import net.liftweb.mongodb.record.BsonMetaRecord

/**
 * Shows how much of a payment is allocated to an Invoice.
 *
 * This is used internally within a [[uk.co.randomcoding.partsdb.core.transaction.Payment]] so no
 * payment reference data is maintained in this record.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class InvoicePayment extends BsonRecord[InvoicePayment] {

  def meta = InvoicePayment

  /**
   * The `ObjectId` of the invoice being paid
   */
  object paidInvoice extends ObjectIdRefField(this, Document)

  /**
   * The amount of the payment that is allocated to this invoice
   */
  object paymentAmount extends DoubleField(this)

  /**
   * Whether or not this payment fully pays the invoice
   *
   * This will only be `true` iff the [[uk.co.randomcoding.partsdb.core.transaction.InvoicePayment]] is a single payment for the full amount of the invoice.
   * Two partial payments that pay the full value of an invoice will both return `false`.
   *
   * @return An `Option[Boolean]` containing `true` or `false` if the invoice is located in the database.
   *         If the invoice cannot be found then `None` is returned. In this case an error should probably be raised and investigated.
   */
  lazy val paidInFull = Document.findById(paidInvoice.get) match {
    case Some(invoice) => Some(paymentAmount.get >= invoice.documentValue)
    case _ => None
  }

  override def equals(that: Any): Boolean = that match {
    case other: InvoicePayment => paidInvoice.get == other.paidInvoice.get && paymentAmount.get == other.paymentAmount.get
    case _ => false
  }

  override def hashCode: Int = getClass.hashCode + paidInvoice.get.hashCode + paymentAmount.get.hashCode
}

object InvoicePayment extends InvoicePayment with BsonMetaRecord[InvoicePayment] {
  /**
   * Create a new payment for an invoice
   */
  def apply(invoice: Document, paymentAmount: Double): InvoicePayment = InvoicePayment.createRecord.paidInvoice(invoice.id.get).paymentAmount(paymentAmount)
}
