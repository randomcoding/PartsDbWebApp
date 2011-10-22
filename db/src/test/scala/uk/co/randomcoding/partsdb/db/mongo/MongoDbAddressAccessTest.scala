/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import uk.co.randomcoding.partsdb.core.address.{ AddressId, Address }
import org.scalatest.matchers.ShouldMatchers

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
class MongoDbAddressAccessTest extends MongoDbTestBase with ShouldMatchers {
  import MongoDbAccess._
  val dbName = "AccessAddressTest"

  def dbAccess = MongoDbAccess(dbName, collectionName)

  test("Add Address") {
    val db = dbAccess
    db.add(Address(AddressId(9876), "Addr1", "Long Addr1", "UK"))

    db.mongoCollection.size should be(1)
  }

  test("Access to Address By Id") {
    val id = AddressId(1234)
    val dbAccess = MongoDbAccess(dbName, collectionName)
    val address = Address(id, "addr", "Long Addr", "UK")
    dbAccess.add(address)

    dbAccess.address(AddressId(1234)) should be(Some(address))
  }

  test("Access to Address that does not exist") {
    val id = AddressId(4321)
    val dbAccess = MongoDbAccess(dbName, collectionName)
    dbAccess.address(id) should be(None)
  }

  test("Access Multiple Addressed with Single Address in DB") {
    val dbAccess = MongoDbAccess(dbName, collectionName)
    val address1 = Address(AddressId(1234), "addr", "Long Addr", "UK")
    dbAccess.add(address1)

    dbAccess.addresses should (contain(address1) and
      have size (1))
  }

  test("Access Multiple Addressed with Two Addresses in DB") {
    val dbAccess = MongoDbAccess(dbName, collectionName)
    val address1 = Address(AddressId(1234), "addr", "Long Addr", "UK")
    val address2 = Address(AddressId(5678), "addr2", "Long Addr 2", "USA")
    dbAccess.add(address1)
    dbAccess.add(address2)

    dbAccess.addresses should (contain(address1) and
      contain(address2) and
      have size (2))
  }

  test("Adding the same address twice results in only one address in the database") {
    val dbAccess = MongoDbAccess(dbName, collectionName)
    val address1 = Address(AddressId(1234), "addr", "Long Addr", "UK")
    dbAccess.add(address1)
    dbAccess.add(address1)

    dbAccess.addresses should (contain(address1) and
      have size (1))
  }
}