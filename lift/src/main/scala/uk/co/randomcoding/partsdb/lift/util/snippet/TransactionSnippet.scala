/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet

import scala.xml.Text

import com.foursquare.rogue.Rogue._

import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.document.DocumentType.DocType
import uk.co.randomcoding.partsdb.core.document.Document
import uk.co.randomcoding.partsdb.core.transaction.Transaction
import uk.co.randomcoding.partsdb.core.util.MongoHelpers._

import net.liftweb.common.Full
import net.liftweb.http.S
import net.liftweb.util.Helpers._

/**
 * Snippet helper to extract and process a [[uk.co.randomcoding.partsdb.core.transaction.Transaction]].
 *
 * This gets the transaction by reading the object id value from the ''transactionId'' parameter
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait TransactionSnippet {

  /**
   * The transaction passed to this shippet identified by a ''transactionId'' parameter
   */
  lazy val transaction = S param "transactionId" match {
    case Full(id) => Transaction findById id
    case _ => None
  }

  /**
   * The name of the transaction and the customer associated to the transaction
   */
  lazy val (transactionName, customer, customerName) = transaction match {
    case Some(t) => (t.shortName, customerFromTransaction(t), customerNameFromTransaction(t))
    case _ => ("No Transaction", None, "No Transaction")
  }

  lazy val transactionDocs: Seq[Document] = transaction match {
    case Some(t) => Document where (_.id in t.documents.get) fetch
    case _ => Nil
  }

  private[this] def customerNameFromTransaction(t: Transaction) = Customer findById t.customer.get match {
    case Some(c) => c.customerName.get
    case _ => "No Customer for id %s in transaction %s".format(t.customer.get, t.shortName)
  }

  private[this] def customerFromTransaction(t: Transaction) = Customer findById t.customer.get match {
    case Some(c) => Some(c)
    case _ => None
  }

  /**
   * Render the `#transactionName` and `#customerName` elements
   */
  def renderTransactionDetails() = {
    "#transactionName" #> Text(transactionName) &
      "#customerName" #> Text(customerName)
  }

  /**
   * Get all the documents with a given [[uk.co.randomcoding.partsdb.core.document.DocumentType]] from the transaction
   */
  def documentsOfType(docType: DocType): Seq[Document] = transactionDocs filter (_.documentType.get == docType)
}