/**
 *
 */
package uk.co.randomcoding.partsdb.core.transaction

import java.util.Date
import org.bson.types.ObjectId
import com.foursquare.rogue.Rogue._
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.document.Document
import net.liftweb.mongodb.record.field._
import net.liftweb.mongodb.record.{ MongoRecord, MongoMetaRecord }
import uk.co.randomcoding.partsdb.core.document.DocumentType
import org.joda.time.DateTime

/**
 * Encapsulates all the data for a transaction between the company and a customer.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class Transaction private () extends MongoRecord[Transaction] with ObjectIdPk[Transaction] {
  def meta = Transaction

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
   * If there is no value here, then the transaction is still active.
   */
  object completionDate extends DateField(this)

  /**
   * Two documents are `equal` if they are for the same customer (determined by the same `oid`) and
   * contain the same documents (again, by `oid`)
   */
  override def equals(that: Any): Boolean = that match {
    case other: Transaction => customer.get == other.customer.get && documents.get.toSet == other.documents.get.toSet
    case _ => false
  }

  override def hashCode: Int = getClass.toString.hashCode + customer.get.hashCode + (documents.get map (_ hashCode) sum)

  lazy val transactionState = {
    new DateTime(completionDate.get) isAfter new DateTime(creationDate.get) match {
      case true => "Completed"
      case false => {
        val docs = documents.get map (Document.findById(_)) filter (_ isDefined) map (_.get)
        val quoteCount = docs.filter(_.documentType.get == DocumentType.Quote).size
        val orderCount = docs.filter(_.documentType.get == DocumentType.Order).size
        val invoiceCount = docs.filter(_.documentType.get == DocumentType.Invoice).size
        val deliveryCount = docs.filter(_.documentType.get == DocumentType.DeliveryNote).size
        (quoteCount, orderCount, (invoiceCount + deliveryCount)) match {
          case (quote, 0, 0) => "Quoted"
          case (_, order, 0) if order > 0 => "Ordered"
          case (_, _, invoice) if invoice > 0 => "Invoiced"
        }
      }
    }
  }
}

object Transaction extends Transaction with MongoMetaRecord[Transaction] {

  import com.foursquare.rogue.Rogue._
  import org.bson.types.ObjectId

  /**
   * Create a new transaction object, but '''do not''' save it in the database
   */
  def create(customer: Customer, documents: Seq[Document]): Transaction = Transaction.createRecord.customer(customer.id.get).documents(documents.toList map (_.id.get))

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
  def add(customer: Customer, documents: Seq[Document]): Option[Transaction] = add(create(customer, documents))

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
}

