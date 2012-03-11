/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import org.bson.types.ObjectId

import com.foursquare.rogue.Rogue._

import uk.co.randomcoding.partsdb.core.document.{ LineItem, DocumentType, DocumentId, Document }
import uk.co.randomcoding.partsdb.core.part.Part
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class DocumentRecordTest extends MongoDbTestBase {
  override val dbName = "DocumentRecordTest"

  val part1 = Part.create("Part 1", Vehicle.create("Vehicle 1"), None)
  val part2 = Part.create("Part 2", Vehicle.create("Vehicle 1"), None)

  val line1 = LineItem.create(1, part1, 1, 10, 0.1)
  val line2 = LineItem.create(2, part2, 2, 20, 0.2)

  test("Create doucment with no line items throws expected exception") {

    val ex = intercept[IllegalArgumentException] {
      Document.create(List.empty[LineItem], DocumentType.Invoice, 0.0)
    }

    ex.getMessage should be("requirement failed: Line Items Cannot be empty")
  }

  test("Equality and HashCode of doucments with line items") {
    val items = Seq(line1, line2)
    val doc1 = Document.create(items, DocumentType.Invoice, 0.0)
    val doc2 = Document.create(items, DocumentType.Invoice, 0.0)
    val doc3 = Document.create(items, DocumentType.Invoice, 0.0)

    doc1 should (equal(doc2) and equal(doc3))
    doc2 should (equal(doc1) and equal(doc3))
    doc3 should (equal(doc1) and equal(doc2))

    doc1.hashCode should (equal(doc2.hashCode) and equal(doc3.hashCode))

    val doc4 = Document.create(items, DocumentType.Order, 0.0)

    doc1 should not equal (doc4)
    doc4 should not equal (doc1)
  }

  test("Equality and HashCode of doucments with document numbers") {
    val items = Seq(LineItem.create(1, part1, 1, 10.0, 0.1), LineItem.create(2, part2, 1, 100.0, 0.1))
    val doc1 = Document.create(items, DocumentType.Invoice, 0.0).docNumber(10)
    val doc2 = Document.create(items, DocumentType.Invoice, 0.0).docNumber(10)
    val doc3 = Document.create(items, DocumentType.Invoice, 0.0).docNumber(10)

    doc1 should (equal(doc2) and equal(doc3))
    doc2 should (equal(doc1) and equal(doc3))
    doc3 should (equal(doc1) and equal(doc2))

    doc1.hashCode should (equal(doc2.hashCode) and equal(doc3.hashCode))

    val doc4 = Document.create(items, DocumentType.Order, 0.0).docNumber(10)
    val doc5 = Document.create(items, DocumentType.Invoice, 0.0).docNumber(20)

    doc1 should (not equal (doc4) and not equal (doc5))
    doc4 should (not equal (doc1) and not equal (doc5))
    doc5 should (not equal (doc1) and not equal (doc4))
  }

  test("Adding a new Record generates a new record if there is no matching record") {
    val expectedDocument1 = Document.create(Seq(line1), DocumentType.Invoice, 0.0).docNumber(1l)
    Document.add(Seq(line1), DocumentType.Invoice, 0.0) should be(Some(expectedDocument1))

    (Document where (_.id exists true) fetch) should be(Seq(expectedDocument1))
    val document2 = Document.create(Seq(line1), DocumentType.Invoice, 0.0).docNumber(2l)
    Document.add(document2) should be(Some(document2))

    (Document where (_.id exists true) fetch) should (have size (2) and
      contain(expectedDocument1) and
      contain(document2))
  }

  test("Adding a new Record that matches an existing Record returns the matched record and does not add a new record to the database") {
    val document = Document.create(Seq(line2), DocumentType.Invoice, 0.0).docNumber(1l)
    val copyDocument = Document.create(Seq(line2), DocumentType.Invoice, 0.0).docNumber(1l)
    Document.add(document) should be(Some(document))
    (Document where (_.id exists true) fetch) should be(List(document))

    Document.add(copyDocument) should be(Some(document))

    (Document where (_.id exists true) fetch) should be(List(document))
  }

  test("Adding new documents correctly increments the document number") {
    var count: Long = 1
    DocumentType.values foreach (docType => {
      Document.add(Seq(line1), docType, 0.0).get.docNumber.get should be(count)
      count += 1
    })
  }

  test("Adding and then removing documents does not affect the steady increment of document number") {
    val d1Id = Document.add(Seq(line1), DocumentType.Invoice, 0.0).get.id.get
    (DocumentId.where(_.id exists true).get.get.currentId.get) should be(1)

    Document.add(Seq(line2), DocumentType.Invoice, 0.0)
    (DocumentId.where(_.id exists true).get.get.currentId.get) should be(2)

    Document.remove(d1Id)

    Document.add(Seq(line1, line2), DocumentType.Order, 0.0).get.id.get

    (DocumentId.where(_.id exists true).get.get.currentId.get) should be(3)
  }

  test("Find Matching returns the correct record if the Object Id matches") {
    val document1 = Document.add(Seq(line1), DocumentType.Invoice, 0.0).get
    Document.findMatching(document1) should be(Some(document1))
  }

  test("Find Matching correctly identifies matches based on object content") {
    val document1 = Document.add(Seq(line2), DocumentType.Invoice, 0.0).get
    document1.docNumber.get should be(1l)
    val matchingDocument = Document.create(Seq(line2), DocumentType.Invoice, 0.0).docNumber(1l)

    Document.findMatching(matchingDocument) should be(Some(document1))

    val notMatchingDocument = Document.create(Seq(line1), DocumentType.Invoice, 0.0).docNumber(10l)

    Document.findMatching(notMatchingDocument) should be('empty)
  }

  test("Removing a Record that exists in the database successfully removes the entry from the database") {
    val document1 = Document.add(Seq(line2), DocumentType.Invoice, 0.0).get
    val document2 = Document.add(Seq(line1), DocumentType.Invoice, 0.0).get

    Document.remove(document1.id.get)

    (Document where (_.id exists true) fetch) should be(List(document2))
  }

  test("Removing a Record from an empty database does not cause errors") {
    Document.remove(new ObjectId)
  }

  test("Removing a that does not exist from a populated database does not cause errors and does not remove any other records") {
    val document1 = Document.add(Seq(line2), DocumentType.Invoice, 0.0).get
    val document2 = Document.add(Seq(line1), DocumentType.Invoice, 0.0).get
    Document.remove(new ObjectId)

    (Document where (_.id exists true) fetch) should (have size (2) and
      contain(document1) and
      contain(document2))
  }

  test("Find a Record that is present in the database by Object Id and document number") {
    val document1 = Document.add(Seq(line2), DocumentType.Invoice, 0.0).get
    val document2 = Document.add(Seq(line1), DocumentType.Invoice, 0.0).get

    Document.findById(document1.id.get) should be(Some(document1))
    Document.findById(document2.id.get) should be(Some(document2))
    Document.findById(new ObjectId) should be(None)

    Document.findByDocumentNumber(1) should be(Some(document1))
    Document.findByDocumentNumber(2) should be(Some(document2))
    Document.findByDocumentNumber(100) should be(None)
  }

  test("Modify a Record with all new values correctly updates the database") {
    pending
  }

  test("Modify a Record does not modify its object id") {
    pending
  }

  private[this] implicit def longToDocumentId(l: Long): DocumentId = DocumentId.createRecord.currentId(l)
}