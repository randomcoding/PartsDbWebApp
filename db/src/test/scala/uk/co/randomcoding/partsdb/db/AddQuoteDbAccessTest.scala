/**
 *
 */
package uk.co.randomcoding.partsdb.db

import uk.co.randomcoding.partsdb.core.document.LineItem
import uk.co.randomcoding.partsdb.core.id.Identifier
import uk.co.randomcoding.partsdb.db.mongo.MongoDbTestBase
import uk.co.randomcoding.partsdb.core.document.Document
import uk.co.randomcoding.partsdb.core.document.DocumentType
import uk.co.randomcoding.partsdb.core.transaction.Transaction
import com.mongodb.casbah.Imports._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddQuoteDbAccessTest extends MongoDbTestBase {
  val dbName = "QuoteHolderDbAccessTest"

  lazy val databaseName = dbName
  lazy val collName = collectionName

  private lazy val access = new DbAccess {
    override val dbName = databaseName
    override val collectionName = collName
  }

  test("Adding Quote successfully adds the quote and transaction objects") {
    val customerId = Identifier(999)
    val expectedDocumentId = Identifier(0)
    val expectedTransactionId = Identifier(1)
    val items = List(LineItem(0, Identifier(100), 2, 10.0, 0.25), LineItem(1, Identifier(20), 1, 15.0, 0.25))
    val added = access.addQuote(items, customerId)

    val expectedQuote = Document(expectedDocumentId, DocumentType.Quote, items, expectedTransactionId)
    val expectedTransaction = Transaction(expectedTransactionId, customerId, Some(Set(expectedDocumentId)))
    added._1.get should be((expectedQuote))
    added._2.get should be((expectedTransaction))

    access.getMatching[Document](MongoDBObject("documentType" -> DocumentType.Quote)) should be(List(expectedQuote))

    access.getMatching[Transaction](("transactionId" $exists true) ++ ("documents" $exists true)) should be(List(expectedTransaction))
  }

}