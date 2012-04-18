/**
 *
 */
package uk.co.randomcoding.partsdb.core.transaction

import java.util.Date

import com.foursquare.rogue.Rogue._

import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.document.{ DocumentType, Document }

import net.liftweb.record.field._
import net.liftweb.mongodb.record.field._
import net.liftweb.mongodb.record.{ MongoRecord, MongoMetaRecord }

import org.joda.time.DateTime
import net.liftweb.common.Logger

/**
 * Encapsulates all the data for a transaction between the company and a customer.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class Transaction private () extends MongoRecord[Transaction] with ObjectIdPk[Transaction] {
  def meta = Transaction

  private val defaultCompletionDate = new Date(0)

  object shortName extends StringField(this, 50)

  /**
   * The customer that this transaction is with.
   *
   * This is managed as an id reference to ensure that updates to the
   * customer record are propagated.
   */
  object customer extends ObjectIdRefField(this, Customer)

  /**
   * The documents that currently make up this transaction.
   */
  object documents extends ObjectIdRefListField(this, Document)

  /**
   * The date that this transaction was created.
   *
   * Has a default value of the `Date` the Transaction was created
   */
  object creationDate extends DateField(this) {
    override def defaultValue = new Date()
  }

  /**
   * The date this transaction was completed.
   *
   * Default value is `new Date(0)`
   */
  object completionDate extends DateField(this) {
    override val defaultValue = defaultCompletionDate
  }

  /**
   * Two documents are `equal` if they are for the same customer (determined by the same `oid`) and
   * contain the same documents (again, by `oid`)
   */
  override def equals(that: Any): Boolean = that match {
    case other: Transaction => customer.get == other.customer.get && documents.get.toSet == other.documents.get.toSet && shortName.get == other.shortName.get
    case _ => false
  }

  override def hashCode: Int = getClass.toString.hashCode + customer.get.hashCode + (documents.get map (_ hashCode) sum) + shortName.get.hashCode

  /**
   * Get the value of all the documents of a given type in this transaction
   *
   * @param documentType The [[uk.co.randomcoding.partsdb.core.document.DocumentType]] to get the total value of
   * @return The total of the `documentValue`s of all the documents of the given type from this transaction
   */
  def valueOfDocuments(documentType: DocumentType.DocType) = (Document where (_.id in documents.get) and (_.documentType eqs documentType) fetch).foldLeft(0.0d)(_ + _.documentValue)

  /**
   * Get the state of the transaction.
   *
   * Can be one of
   * - '''Completed'''
   * - '''Quoted'''
   * - '''Ordered'''
   * - '''Delivered'''
   * - '''Invoiced'''
   */
  lazy val transactionState = {
    completionDate.get == defaultCompletionDate match {
      case false => "Completed"
      case true => {
        val docs = documents.get map (Document.findById(_)) filter (_ isDefined) map (_.get)
        val quoteCount = docs.filter(_.documentType.get == DocumentType.Quote).size
        val orderCount = docs.filter(_.documentType.get == DocumentType.Order).size
        val invoiceCount = docs.filter(_.documentType.get == DocumentType.Invoice).size
        val deliveryCount = docs.filter(_.documentType.get == DocumentType.DeliveryNote).size
        (quoteCount, orderCount, deliveryCount, invoiceCount) match {
          case (quote, 0, 0, 0) => "Quoted"
          case (_, order, 0, 0) if order > 0 => "Ordered"
          case (_, _, deliver, 0) if deliver > 0 => "Delivered"
          case (_, _, _, invoice) if invoice > 0 => "Invoiced"
        }
      }
    }
  }
}

object Transaction extends Transaction with MongoMetaRecord[Transaction] with Logger {

  import com.foursquare.rogue.Rogue._
  import org.bson.types.ObjectId

  /**
   * Create a new transaction object, but '''do not''' save it in the database
   */
  def create(shortName: String, customer: Customer, documents: Seq[Document]): Transaction = Transaction.createRecord.shortName(shortName).customer(customer.id.get).documents(documents.toList map (_.id.get))

  /**
   * Add a new `Transaction` to the database.
   *
   * If there is a `Transaction` that matches then this transaction will be returned and '''no''' add operation will
   * be attempted. Otherwise the transaction will be added to the database.
   *
   * @see [[uk.co.randomcoding.partsdb.core.transaction.Transaction#findMatching(Transaction)]])
   * @return A populated `Option[Transaction]` with either the matched or newly added record, if the add operation succeeded. Otherwise 'none'
   */
  def add(transaction: Transaction): Option[Transaction] = findMatching(transaction) match {
    case Some(t) => Some(t)
    case _ => transaction.save match {
      case tr: Transaction => Some(tr)
      case _ => None
    }
  }

  /**
   * Add a new `Transaction` to the database.
   *
   * If there is a `Transaction` that matches then this transaction will be returned and '''no''' add operation will
   * be attempted. Otherwise the transaction will be added to the database.
   *
   * @see [[uk.co.randomcoding.partsdb.core.transaction.Transaction#findMatching(Transaction)]])
   * @return A populated `Option[Transaction]` with either the matched or newly added record, if the add operation succeeded. Otherwise 'none'
   */
  def add(shortName: String, customer: Customer, documents: Seq[Document]): Option[Transaction] = add(create(shortName, customer, documents))

  /**
   * Find a `Transaction` by its `oid`.
   *
   * @return A populated `Option[Transaction]` if there is a `Transaction` with the given `oid` or `None` otherwise
   */
  def findById(oid: ObjectId): Option[Transaction] = Transaction where (_.id eqs oid) get

  /**
   * A document matches if the customer field is the same customer `oid` and the documents field contains '''all''' the same
   * `oid`s as the document field of `transaction`.
   *
   * If a document record has the same customer and a subset of the same document records then there is no match
   *
   * == Example ==
   *
   * {{{
   * val t1 = Transaction.add(cust1, Seq(doc1, doc2, doc3)) // Add the transaction into the db
   *
   * val subsetTransaction = Transaction.create(cust1, Seq(doc1, doc3)) // only contains a subset of docs so should not match
   *
   * val match1 = Transaction.findMatching(subsetTransaction) // This should be Some(t1) as t1 contains all the docs of subset plus more
   *
   * val supersetTransaction = Transaction.create(cust1, Seq(doc1, doc4, doc2, doc3)) // order of the docs is not important
   *
   * val match2 = Transaction.findMatching(supersetTransaction) // this should be None as t1 contains fewer docs that superset
   * }}}
   */
  def findMatching(transaction: Transaction): Option[Transaction] = findById(transaction.id.get) match {
    case Some(t) => Some(t)
    case _ => Transaction where (_.customer eqs transaction.customer.get) and (_.documents all transaction.documents.get) get
  }

  /**
   * Adds document(s) by Id to a transaction
   *
   * @param transactionId The oid of the `Transaction` to add the document(s) to
   * @param documentId The id(s) of the document(s) to add to the transaction
   */
  def addDocument(transactionId: ObjectId, documentId: ObjectId*) {
    val docIds = findById(transactionId) match {
      case Some(t) => (t.documents.get ++ documentId).distinct
      case _ => Nil // If this is the case then the update operation will do nothing so Nil is safe
    }
    Transaction.where(_.id eqs transactionId).modify(_.documents setTo docIds).updateMulti
  }

  /**
   * Mark a transaction as closed.
   *
   * This sets the `completionDate` to the current date.
   *
   * @param oid The `ObjectId` of the transaction to close
   * @return The Modified transaction, or None if the transaction is not found in the database
   */
  def close(oid: ObjectId): Option[Transaction] = {
    val now = DateTime.now
    debug("Closing transaction with id %s, setting completion date to %s".format(oid, now))
    debug("There are %d transactions to modify".format(Transaction.where(_.id eqs oid).count))
    Transaction where (_.id eqs oid) modify (_.completionDate setTo now.toDate) updateMulti

    debug("Modified Transaction: %s".format(findById(oid)))
    findById(oid)
  }
}

