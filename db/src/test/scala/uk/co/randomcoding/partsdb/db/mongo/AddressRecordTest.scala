/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import com.foursquare.rogue.Rogue._
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle._
import org.bson.types.ObjectId

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddressRecordTest extends MongoDbTestBase {
  import uk.co.randomcoding.partsdb.core.address.Address._
  import uk.co.randomcoding.partsdb.core.address.Address

  override val dbName = "AddressRecordTest"

  test("Adding a single address works ok") {
    val addr = add("Addr1", "An Address", "UK")

    val expectedAddress = createRecord.shortName("Addr1").addressText("An Address").country("UK")

    addr should be(Some(expectedAddress))

    findNamed("Addr1") should be(List(expectedAddress))

    findById(addr.get.id.get) should be(Some(expectedAddress))
  }

  test("Adding the same address more than once does not result in a duplication, or a change of underlying id") {
    val addr = add("Addr1", "An Address", "UK")
    addr should be('defined)
    add("Addr1", "An Address", "UK") should be(addr)

    val expectedAddress = createRecord.shortName("Addr1").addressText("An Address").country("UK")

    (Address where (_.id exists true) fetch) should be(List(expectedAddress))

    findNamed("Addr1")(0).id.get should be(addr.get.id.get)
  }

  test("Adding an address with the same name but different other details as an already existing address returns the original record") {
    val addr = add("Addr1", "An Address", "UK").get

    add("Addr1", "ANother Address", "UR") should be(Some(addr))
    add("Addr1", "ANother Address", "UR").get.id.get should be(addr.id.get)
  }

  test("Adding an address with the same address text but different other details as an already existing address returns the original record") {
    val addr = add("Addr1", "An Address", "UK").get

    add("Addr2", "An Address", "UR") should be(Some(addr))
    add("Addr2", "An Address", "UR").get.id.get should be(addr.id.get)
  }

  test("Find Matching can find a matching record by Object Id") {
    val addr = add("Addr1", "An Address", "UK").get

    val otherAddress = createRecord.id(addr.id.get).shortName("Addr2").addressText("Another Text").country("ZR")

    findMatching(otherAddress) should be(Some(addr))
  }

  test("Find Matching can find a matching record by address name or address text") {
    val addr = add("Addr1", "An Address", "UK")
    val otherAddress1 = createRecord.shortName("Addr1").addressText("Another Text").country("ZR")
    val otherAddress2 = createRecord.shortName("Addr2").addressText("An Address").country("ZR")

    findMatching(otherAddress1) should be(addr)
    findMatching(otherAddress2) should be(addr)
  }

  test("Removing an address") {
    val addr1 = add("Addr1", "An Address", "UK").get
    val addr2 = add("Addr2", "Another Address", "UK")

    val addr1Id = addr1.id.get
    remove("Addr1") should be(List(true))

    findNamed("Addr1") should be('empty)
    findById(addr1Id) should be('empty)

    (Address where (_.id exists true) fetch) should be(List(addr2.get))
  }

  test("Removing an address from an empty database") {
    remove("Addr123") should be(Nil)
    remove("Addr456") should be(Nil)
  }

  test("Find an address that is present in the database") {
    val addr1 = add("Address1", "An Address Text", "UK").get

    findNamed("Address1") should be(List(addr1))

    findById(addr1.id.get) should be(Some(addr1))

    findByAddressText("An Address Text") should be(List(addr1))
  }

  test("Find an address that is not present in the database") {
    val addr1 = add("Address1", "An Address Text", "UK").get

    findNamed("Address2") should be(Nil)

    findById(new ObjectId("4f2871f4231823ddb82a080c")) should be('empty)

    findByAddressText("Another Address Text") should be(Nil)
  }

  test("Modify an address") {
    val origAddr = add("Address 1", "An Address Text", "UK").get
    val origId = origAddr.id.get

    modify(origAddr.id.get, "Address 1-1", "Modified Address Text", "United Kingdom")

    findNamed("Address 1") should be(Nil)
    findNamed("Address 1-1") should be(List(createRecord.shortName("Address 1-1").addressText("Modified Address Text").country("United Kingdom")))
  }

  test("Modfy an address does not modify its object id") {
    val origAddr = add("Address 1", "An Address Text", "UK").get
    val origId = origAddr.id.get

    modify(origAddr.id.get, "Address 1-1", "Modified Address Text", "United Kingdom")

    findNamed("Address 1") should be(Nil)
    findById(origId) should be(Some(createRecord.shortName("Address 1-1").addressText("Modified Address Text").country("United Kingdom")))
  }
}