/*
 * Copyright (C) 2012 RandomCoder <randomcoder@randomcoding.co.uk>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *    RandomCoder - initial API and implementation and/or initial documentation
 */
package uk.co.randomcoding.partsdb.db.mongo

import com.foursquare.rogue.Rogue._
import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.contact.ContactDetails
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.part.Part
import uk.co.randomcoding.partsdb.core.transaction.Transaction
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle
import org.bson.types.ObjectId
import uk.co.randomcoding.partsdb.core.document.{ Quote, LineItem, DocumentType, Document }
import uk.co.randomcoding.partsdb.core.supplier.Supplier

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class TransactionRecordTest extends MongoDbTestBase {
  override val dbName = "TransactionRecordTest"

  private[this] val addr1 = Address.create("Addr1", "Addr1", "UK")
  private[this] val addr2 = Address.create("Addr2", "Addr2", "UK")
  private[this] val contacts1 = ContactDetails.create("Dave", "", "", "", "", true)
  private[this] val contacts2 = ContactDetails.create("Alan", "", "", "", "", true)
  private[this] val cust1 = Customer.create("Customer 1", addr1, 30, contacts1)
  private[this] val cust2 = Customer.create("Customer 2", addr2, 45, contacts2)

  private[this] val supplier = Supplier("Supplier", ContactDetails("Dave", "", "", "", "", true), Address("Addr1", "Address 1", "UK"), Nil)

  private[this] val line1 = LineItem.create(1, Part.create("Part", Vehicle.create("Vehicle"), None), 1, 10.0, 0.1, supplier)
  private[this] val lines = Seq(line1)
  /*
   * Setting the id for doc1 avoids a funny hash collision problem where the object ids generated for
   * doc1 & cust2 and doc2 & cust1 are effectively sequential. This results in hash code collisions where
   * the object ids of the two pairs have the same interval.
   *
   * E.g. oid (hash)
   * Cust1 id: 4f37b14023182cd492f528d7 (1224398707)
   * Doc2  id: 4f37b14023182cd492f528dc (1224398792)
   *
   * Cust2 id: 4f37b14023182cd492f528db (1224398775)
   * Doc1  id: 4f37b13e23182cd492f528d6 (1224398688)
   *
   * As only the last two digits change, and the two pairs add to the same then they will collide on hash code - GRR
   */
  val doc1 = Document.create(lines, DocumentType.Invoice, 0.0).docNumber(1001).id(new ObjectId)
  val doc3 = Document.create(lines, DocumentType.Quote, 0.0).docNumber(3003)
  val doc2 = Document.create(lines, DocumentType.Order, 0.0).docNumber(2002)

  test("Equality and HashCode") {
    val t1 = Transaction.create(cust1, Seq(doc1))
    val t2 = Transaction.create(cust1, Seq(doc1))
    val t3 = Transaction.create(cust1, Seq(doc1))

    t1 should (equal(t2) and equal(t3))
    t2 should (equal(t1) and equal(t3))
    t3 should (equal(t1) and equal(t2))

    t1.hashCode should (be(t2.hashCode) and be(t3.hashCode))

    val t4 = Transaction.create(cust2, Seq(doc1))
    val t5 = Transaction.create(cust1, Seq(doc2))
    val t6 = Transaction.create(cust1, Seq(doc1))

    t4 should (not equal (t1) and not equal (t5) and not equal (t6))
    t5 should (not equal (t1) and not equal (t4) and not equal (t6))
    t6 should (not equal (t4) and not equal (t5))

    t4.hashCode should (not be (t5.hashCode) and not be (t6.hashCode))
    t5.hashCode should not be (t6.hashCode)
  }

  test("Adding a new Record generates a new record if there is no matching record") {
    val t1 = Transaction.add(cust1, Seq(doc1)).get
    (Transaction fetch) should be(List(t1))

    val t2 = Transaction.add(cust2, Seq(doc2)).get
    (Transaction fetch) should (have size (2) and
      contain(t1) and
      contain(t2))
  }

  test("Adding a new Record that matches an existing Record returns the matched record and does not add a new record to the database") {
    val t1 = Transaction.add(cust1, Seq(doc1)).get
    val t2 = Transaction.add(cust1, Seq(doc1)).get

    t2 should be(t1)
    (Transaction fetch) should be(List(t1))
  }

  test("Find Matching returns the correct record if the Object Id matches") {
    val t1 = Transaction.add(cust1, Seq(doc1)).get
    val t2 = Transaction.create(cust2, Seq(doc2)).id(t1.id.get)

    Transaction.findMatching(t2) should be(Some(t1))

    val t3 = Transaction.create(cust2, Seq(doc2))
    Transaction.findMatching(t3) should be(None)
  }

  test("Find Matching correctly identifies matches based on object content") {
    val t1 = Transaction.add(cust1, Seq(doc1)).get

    val t1matching1 = Transaction.create(cust1, Seq(doc1))
    Transaction.findMatching(t1matching1) should be(Some(t1))

    val t1NotMatching1 = Transaction.create(cust2, Seq(doc1))
    Transaction.findMatching(t1NotMatching1) should be(None)

    val t1NotMatching2 = Transaction.create(cust1, Seq(doc2))
    Transaction.findMatching(t1NotMatching2) should be(None)

    val t1NotMatching3 = Transaction.create(cust1, Nil)
    Transaction.findMatching(t1NotMatching3) should be(None)

    val t2 = Transaction.add(cust2, Seq(doc1, doc2, doc3)).get

    val t2matching1 = Transaction.create(cust2, Seq(doc1, doc2, doc3))
    Transaction.findMatching(t2matching1) should be(Some(t2))

    val t2matching2 = Transaction.create(cust2, Seq(doc2, doc1, doc3))
    Transaction.findMatching(t2matching2) should be(Some(t2))

    val t2notMatching1 = Transaction.create(cust1, Seq(doc1, doc3, doc2))
    Transaction.findMatching(t2notMatching1) should be(None)

    val t2notMatching2 = Transaction.create(cust1, Seq(doc3, doc2, doc1))
    Transaction.findMatching(t2notMatching2) should be(None)
  }

  test("Find Matching correctly matches database records that contain a superset of the documents of the match request") {
    val t2 = Transaction.add(cust2, Seq(doc1, doc2, doc3)).get

    val t2subsetMatching1 = Transaction.create(cust2, Seq(doc1, doc2))
    Transaction.findMatching(t2subsetMatching1) should be(Some(t2))

    val t2subsetMatching2 = Transaction.create(cust2, Seq(doc2, doc1))
    Transaction.findMatching(t2subsetMatching2) should be(Some(t2))

    val t2subsetMatching3 = Transaction.create(cust2, Seq(doc3, doc2))
    Transaction.findMatching(t2subsetMatching3) should be(Some(t2))

    val t2subsetMatching4 = Transaction.create(cust2, Seq(doc3, doc1))
    Transaction.findMatching(t2subsetMatching4) should be(Some(t2))

    val t2subsetMatching5 = Transaction.create(cust2, Seq(doc1))
    Transaction.findMatching(t2subsetMatching5) should be(Some(t2))

    val t2subsetMatching6 = Transaction.create(cust2, Seq(doc2))
    Transaction.findMatching(t2subsetMatching6) should be(Some(t2))

    val t2subsetMatching7 = Transaction.create(cust2, Seq(doc3))
    Transaction.findMatching(t2subsetMatching7) should be(Some(t2))
  }

  test("Find Matching correctly does not match database records that contain only a subset of the documents of the match request") {
    val t1 = Transaction.add(cust1, Seq(doc1)).get
    val t2 = Transaction.add(cust2, Seq(doc1, doc2)).get

    val t1matching1 = Transaction.create(cust1, Seq(doc1, doc2))
    Transaction.findMatching(t1matching1) should be(None)

    val t1matching2 = Transaction.create(cust1, Seq(doc2, doc1))
    Transaction.findMatching(t1matching2) should be(None)

    val t2supersetNotMatching1 = Transaction.create(cust2, Seq(doc1, doc2, doc3))
    Transaction.findMatching(t2supersetNotMatching1) should be(None)
  }

  test("Add a single document") {
    val t1 = Transaction.add(cust1, Seq(doc1)).get

    Transaction.addDocument(t1.id.get, doc2.id.get)

    Transaction.findById(t1.id.get).get.documents.get should (have size (2) and
      contain(doc1.id.get) and
      contain(doc2.id.get))
  }

  test("Adding a document that is already present does not result in duplicate entries") {
    val t1 = Transaction.add(cust1, Seq(doc1)).get

    Transaction.addDocument(t1.id.get, doc1.id.get)

    Transaction.findById(t1.id.get).get.documents.get should be(Seq(doc1.id.get))
  }

  test("Adding multiple new documents") {
    val t1 = Transaction.add(cust1, Seq(doc1)).get

    Transaction.addDocument(t1.id.get, doc2.id.get, doc3.id.get)

    Transaction.findById(t1.id.get).get.documents.get should (have size (3) and
      contain(doc1.id.get) and
      contain(doc2.id.get) and
      contain(doc3.id.get))
  }

  test("Adding multiple documents, some new and somw duplicates") {
    val t1 = Transaction.add(cust1, Seq(doc1)).get

    Transaction.addDocument(t1.id.get, doc1.id.get, doc2.id.get)

    Transaction.findById(t1.id.get).get.documents.get should (have size (2) and
      contain(doc1.id.get) and
      contain(doc2.id.get))
  }

  test("Generation of Transaction Short Name (id)") {
    val t1 = Transaction.add(cust1, Seq(doc3)).get
    val t2 = Transaction.add(cust1, Seq(doc1)).get
    Document.add(doc3)
    Document.add(doc1)

    t1.shortName should be("TRN003003")
    t2.shortName should startWith("No Quote for Transaction ")
  }

  test("Removing a Record that exists in the database successfully removes the entry from the database") {
    pending
  }

  test("Removing a Record from an empty database does not cause errors") {
    pending
  }

  test("Removing a that does not exist from a populated database does not cause errors and does not remove any other records") {
    pending
  }

  test("Find a Record that is present in the database by Object Id and name and...") {
    pending
  }

  test("Find a Record that is not present in the database does not cause errors") {
    pending
  }

  test("Modify a Record with all new values correctly updates the database") {
    pending
  }

  test("Modify a Record does not modify its object id") {
    pending
  }

}
