/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import uk.co.randomcoding.partsdb.core.document.DocumentId
import com.foursquare.rogue.Rogue._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class DocumentIdRecordTest extends MongoDbTestBase {
  override val dbName = "DocumentIdRecordTest"

  test("First Call to next id returns id 1") {
    DocumentId.nextId.currentId.get should be(1l)
  }

  test("Sequential calls to next id return the correct sequence of numbers") {
    (1 to 10000) foreach (index => DocumentId.nextId.currentId.get should be(index))
  }

  test("Multiple calls to next id do not create multiple records in the database") {
    DocumentId.nextId.currentId.get should be(1l)
    (DocumentId fetch) should be(List(DocumentId.createRecord.currentId(1)))
    DocumentId.nextId.currentId.get should be(2l)
    (DocumentId fetch) should be(List(DocumentId.createRecord.currentId(2)))
    DocumentId.nextId.currentId.get should be(3l)
    (DocumentId fetch) should be(List(DocumentId.createRecord.currentId(3)))
    DocumentId.nextId.currentId.get should be(4l)
    (DocumentId fetch) should be(List(DocumentId.createRecord.currentId(4)))
  }
}