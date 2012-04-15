/**
 *
 */
package uk.co.randomcoding.partsdb.core.transaction

import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.record.field.{ DoubleField, StringField }
import net.liftweb.mongodb.record.field._
import java.util.Date
import com.foursquare.rogue.Rogue._
import org.bson.types.ObjectId
import uk.co.randomcoding.partsdb.core.document.{ DocumentType, Document }

/**
 * Associates a payment from a customer, as identified by a date and reference (from a statement or cheque)
 * to the invoices that were paid and how much of the balance was paid.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class Payment private () extends MongoRecord[Payment] with ObjectIdPk[Payment] {
  def meta = Payment

  object paymentAmount extends DoubleField(this)

  object paymentDate extends DateField(this)

  object paymentReference extends StringField(this, 50)

  object paidInvoices extends BsonRecordListField(this, InvoicePayment)

  final def isFullyAllocated: Boolean = {
    paidInvoices.get.map(_.paymentAmount.get).sum >= paymentAmount.get
  }

  final def unallocatedBalance: Double = isFullyAllocated match {
    case true => 0d
    case false => paymentAmount.get - (actualInvoices.foldLeft(0.0d)(_ + _.documentValue))
  }

  private[this] def actualInvoices = Document where (_.id in (paidInvoices.get map (_.paidInvoice.get))) and (_.documentType eqs DocumentType.Invoice) fetch

  override def equals(that: Any): Boolean = that match {
    case other: Payment => {
      paymentAmount.get == other.paymentAmount.get &&
        paymentReference.get == other.paymentReference.get &&
        paidInvoices.get == other.paidInvoices.get
    }
    case _ => false
  }

  override def hashCode: Int = getClass.hashCode + paymentAmount.get.hashCode + paymentReference.get.hashCode + paidInvoices.get.hashCode
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

  /**
   * Find a [[uk.co.randomcoding.partsdb.core.transaction.Payment]] by its Object Id
   *
   * @param oid The Object Id of the [[uk.co.randomcoding.partsdb.core.transaction.Payment]] to find
   * @return An `Option[`[[uk.co.randomcoding.partsdb.core.transaction.Payment]]`]` with the found record if it exists or `None` otherwise
   */
  def findById(oid: ObjectId): Option[Payment] = Payment where (_.id eqs oid) get

  /**
   * Add invoice payments to the payment with the given `paymentId`
   *
   * @param paymentId The Object Id of the payment to modify
   * @param invoicePayments The [[uk.co.randomcoding.partsdb.core.transaction.InvoicePayment]]s to set the
   * @return The modified [[uk.co.randomcoding.partsdb.core.transaction.Payment]] record if it is present in the database.
   *         If there is not a `Payment` with the given `paymentId` then returns `None`
   */
  def addInvoices(paymentId: ObjectId, invoicePayments: Seq[InvoicePayment]): Option[Payment] = {
    findById(paymentId) match {
      case Some(p) => {
        val existingPayments = p.paidInvoices.get
        val paymentsToAdd = (existingPayments ++ invoicePayments).toList.distinct

        Payment where (_.id eqs paymentId) modify (_.paidInvoices setTo paymentsToAdd) updateMulti

        findById(paymentId)
      }
      case _ => None
    }
  }
}
