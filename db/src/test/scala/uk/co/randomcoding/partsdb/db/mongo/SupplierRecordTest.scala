/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import uk.co.randomcoding.partsdb.core.supplier.Supplier
import uk.co.randomcoding.partsdb.core.supplier.Supplier._
import com.foursquare.rogue.Rogue._
import org.bson.types.ObjectId
import uk.co.randomcoding.partsdb.core.contact.ContactDetails
import uk.co.randomcoding.partsdb.core.part.PartCost
import uk.co.randomcoding.partsdb.core.part.Part
import org.joda.time.DateTime._
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle
import uk.co.randomcoding.partsdb.core.address.Address
import org.joda.time.DateTime

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class SupplierRecordTest extends MongoDbTestBase {
  override val dbName = "SupplierRecordTest"

  val contactDave = ContactDetails.create("Dave", "", "", "", "", true)
  val contactSally = ContactDetails.create("Sally", "", "", "", "", true)

  val address1 = Address.create("Addr 1", "Address Text", "UK")
  val address2 = Address.create("Addr 2", "Other Address Text", "UK")

  val part1 = Part.create("Part 1", Vehicle.create("Vehicle 1"), None)

  val partCost1 = PartCost.create(part1, 10.0, now)
  val partCost2 = PartCost.create(part1, 12.0, now)

  def allSuppliers = Supplier where (_.id exists true) fetch

  test("Equality and HashCode") {
    val s1 = create("Supplier 1", contactDave, address1, Seq(partCost1))
    val s2 = create("Supplier 1", contactDave, address1, Seq(partCost1))
    val s3 = create("Supplier 1", contactDave, address1, Seq(partCost1))

    s1 should (equal(s2) and equal(s3))
    s2 should (equal(s1) and equal(s3))
    s3 should (equal(s1) and equal(s2))

    s1.hashCode should (equal(s2.hashCode) and equal(s3.hashCode))
  }

  test("Adding a new Record generates a new record if there is no matching record") {
    val s1 = add("Supplier 1", contactDave, address1, Seq(partCost1)).get

    allSuppliers should be(List(s1))

    val s2 = add("Supplier 2", contactSally, address2, Seq(partCost2)).get

    allSuppliers should (have size (2) and
      contain(s1) and
      contain(s2))
  }

  test("Adding a new Record that matches an existing Record returns the matched record and does not add a new record to the database") {
    val s1 = add("Supplier 1", contactDave, address2, Seq(partCost1)).get
    add("Supplier 1", contactDave, address2, Seq(partCost1)) should be(Some(s1))
    allSuppliers should be(List(s1))
  }

  test("Find Matching returns the correct record if the only the Object Id matches") {
    val s1 = add("Supplier 1", contactDave, address1, Seq(partCost1)).get
    val s2 = Supplier.create("Another Supplier", contactSally, address2, List(PartCost.create(part1, 10.0d, DateTime.now))).id(s1.id.get)

    findMatching(s2) should be(Some(s1))
  }

  test("Find Matching correctly identifies matches based on object content") {
    val s1 = add("Supplier 1", contactDave, address1, Seq(partCost1)).get
    val s2 = create("Supplier 1", contactDave, address2, Seq(partCost1))
    findMatching(s2) should be(Some(s1))
    val s3 = create("Supplier 1", contactSally, address1, Seq(partCost1))
    findMatching(s3) should be(Some(s1))
    val s4 = create("Supplier 1", contactDave, address1, Seq(partCost1))
    findMatching(s4) should be(Some(s1))
    val s5 = create("Supplier 1", contactSally, address2, Seq(partCost1))
    findMatching(s5) should be(None)
  }

  test("Removing a Record that exists in the database successfully removes the entry from the database") {
    val s1 = add("Supplier 1", contactDave, address1, Seq(partCost1)).get
    val s2 = add("Supplier 2", contactSally, address2, Seq(partCost2)).get

    remove(s1.id.get)

    allSuppliers should be(List(s2))
  }

  test("Removing a Record from an empty database does not cause errors") {
    remove(new ObjectId)
  }

  test("Removing a that does not exist from a populated database does not cause errors and does not remove any other records") {
    val s1 = add("Supplier 1", contactDave, address1, Seq(partCost1)).get
    val s2 = add("Supplier 2", contactSally, address2, Seq(partCost2)).get

    remove(new ObjectId)

    allSuppliers should (have size (2) and
      contain(s1) and
      contain(s2))
  }

  test("Find a Record that is present in the database by Object Id and name") {
    val s1 = add("Supplier 1", contactDave, address1, Seq(partCost1)).get
    val s2 = add("Supplier 2", contactSally, address2, Seq(partCost2)).get

    findById(s1.id.get) should be(Some(s1))
    findById(s2.id.get) should be(Some(s2))

    findNamed("Supplier 1") should be(List(s1))
    findNamed("Supplier 2") should be(List(s2))
  }

  test("Find a Record that is not present in the database does not cause errors") {
    val s1 = add("Supplier 1", contactDave, address1, Seq(partCost1)).get
    val s2 = add("Supplier 2", contactSally, address2, Seq(partCost2)).get
    findById(new ObjectId) should be(None)
    findNamed("Supplier 3") should be(Nil)
  }

  test("Modify a Record with all new values correctly updates the Record") {
    val s1 = add("Supplier 1", contactDave, address1, Seq(partCost1)).get
    modify(s1.id.get, "Supplier 10", contactSally, address2, Seq(partCost2), "")

    val expectedSupplier = create("Supplier 10", contactSally, address2, Seq(partCost2))

    allSuppliers should be(List(expectedSupplier))
  }

  test("Modify a Record does not modify its object id") {
    val s1 = add("Supplier 1", contactDave, address1, Seq(partCost1)).get
    modify(s1.id.get, "Supplier 10", contactSally, address2, Seq(partCost2), "")

    val expectedSupplier = create("Supplier 10", contactSally, address2, Seq(partCost2))
    findById(s1.id.get) should be(Some(expectedSupplier))
  }

  // These are only required if this has embedded objects, referenced by Object Id
  test("Adding a new Record adds all embedded records to the database") {
    pending
  }

  test("Modifying a Record with different embedded record ids works correctly, if the new records are already added") {
    pending
  }

  // These tests are 'pending'
  test("Modifying a ObjectRefId field will remove the referenced object if it is no longer used in the database") {
    pending
  }

  test("Modifying a ObjectRefId field will not remove the referenced object if it is still in use in the database") {
    pending
  }
}