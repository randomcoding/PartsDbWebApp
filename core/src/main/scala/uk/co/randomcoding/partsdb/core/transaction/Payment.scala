/**
 *
 */
package uk.co.randomcoding.partsdb.core.transaction

import net.liftweb.mongodb.record.BsonRecord
import net.liftweb.mongodb.record.BsonMetaRecord
import net.liftweb.record.field.{ DoubleField, StringField }
import net.liftweb.mongodb.record.field.DateField
import java.util.Date
import uk.co.randomcoding.partsdb.core.document.Document
import net.liftweb.mongodb.record.field.MongoRefListField
import net.liftweb.mongodb.record.field.ObjectIdRefListField
import net.liftweb.mongodb.record.field.ObjectIdRefField
import net.liftweb.mongodb.record.field.BsonRecordListField

/**
 * Associates a payment from a customer, as identified by a date and reference (from a statement or cheque)
 * to the invoices that were paid and how much of the balance was paid.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class Payment private () extends BsonRecord[Payment] {
  def meta = Payment

  object paymentAmount extends DoubleField(this)

  object paymentDate extends DateField(this)

  object paymentReference extends StringField(this, 50)

  object paidInvoices extends BsonRecordListField(this, InvoicePayment)
}

object Payment extends Payment with BsonMetaRecord[Payment] {

  def apply(paymentAmount: Double, paymentReference: String, paidInvoices: Seq[InvoicePayment], paymentDate: Date = new Date): Payment = create(paymentAmount, paymentReference, paidInvoices, paymentDate)

  /**
   * Create a new instance of a `Payment`
   *
   * @param paymentAmount The amount of the payment
   * @param paymentReference The reference (possibly as it would appear on the statement) of the payment
   * @param paidIncoices The invoices that are fully paid by this payment
   */
  def create(paymentAmount: Double, paymentReference: String, paidInvoices: Seq[InvoicePayment], paymentDate: Date = new Date): Payment = {
    Payment.createRecord.paymentAmount(paymentAmount).paymentDate(paymentDate).paidInvoices(paidInvoices.toList).paymentReference(paymentReference)
  }
}