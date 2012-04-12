/*
 *
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
}

object InvoicePayment extends InvoicePayment with BsonMetaRecord[InvoicePayment] {
  /**
   * Create a new payment for an invoice
   */
  def apply(invoice: Document, paymentAmount: Double): InvoicePayment = InvoicePayment.createRecord.paidInvoice(invoice.id.get).paymentAmount(paymentAmount)
}
