/**
 *
 */
package uk.co.randomcoding.partsdb.core.transaction

import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.record.field.{DoubleField, StringField}
import net.liftweb.mongodb.record.field._
import java.util.Date
import com.foursquare.rogue.Rogue._

/**
 * Associates a payment from a customer, as identified by a date and reference (from a statement or cheque)
 * to the invoices that were paid and how much of the balance was paid.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class Payment private() extends MongoRecord[Payment] with ObjectIdPk[Payment] {
  def meta = Payment

  object paymentAmount extends DoubleField(this)

  object paymentDate extends DateField(this)

  object paymentReference extends StringField(this, 50)

  object paidInvoices extends BsonRecordListField(this, InvoicePayment)

  final def isFullyAllocated: Boolean = {
    paidInvoices.get.map(_.paymentAmount.get).sum >= paymentAmount.get
  }
}

object Payment extends Payment with MongoMetaRecord[Payment] {

  def apply(paymentAmount: Double, paymentReference: String, paidInvoices: Seq[InvoicePayment], paymentDate: Date = new Date): Payment = create(paymentAmount, paymentReference, paidInvoices, paymentDate)

  /**
   * Create a new instance of a `Payment`
   *
   * @param paymentAmount The amount of the payment
   * @param paymentReference The reference (possibly as it would appear on the statement) of the payment
   * @param paidInvoices The invoices that are fully paid by this payment
   */
  def create(paymentAmount: Double, paymentReference: String, paidInvoices: Seq[InvoicePayment], paymentDate: Date = new Date): Payment = {
    Payment.createRecord.paymentAmount(paymentAmount).paymentDate(paymentDate).paidInvoices(paidInvoices.toList).paymentReference(paymentReference)
  }

  /**
   * Add a payment to the database unless there is already a matching payment.
   *
   * @see [[uk.co.randomcoding.partsdb.core.transaction.Payment# f i n d M a t c h i n g ( P a y m e n t )]]
   *
   * @param payment The payment to add to the database
   * @return An option containing the added payment, if the addition was successful. If there was already a matching payment, then the option contains the match instead
   */
  def add(payment: Payment): Option[Payment] = findMatching(payment) match {
    case Some(p) => Some(p)
    case _ => payment.saveTheRecord()
  }

  /**
   * Find a payment that matches the provided one.
   *
   * A payment matches if the `paymentReference` is the same.
   *
   * @param payment The payment to find a match for
   * @return An `Option[Payment]` that is populated if a match is found or `None` if there is no match
   */
  def findMatching(payment: Payment): Option[Payment] = Payment where (_.paymentReference eqs payment.paymentReference.get) get
}
