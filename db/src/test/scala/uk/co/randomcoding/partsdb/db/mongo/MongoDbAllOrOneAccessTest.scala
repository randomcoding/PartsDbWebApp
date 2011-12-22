/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import org.scalatest.matchers.ShouldMatchers
import com.mongodb.casbah.Imports._
import uk.co.randomcoding.partsdb._
import core.address._
import core.part._
import core.id._
import uk.co.randomcoding.partsdb.db.mongo.MongoConverters._
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 * @author Jane Rowe
 *
 */
class MongoDbAllOrOneAccessTest extends MongoDbTestBase with ShouldMatchers {
  override val dbName = "allOrOneTest"

  lazy val dbAccess = new MongoAllOrOneAccess() {
    override val collection = mongo
  }

  // Address Tests
  test("Access to Address By Id") {
    val address = Address(Identifier(1234), "addr", "Long Addr", "UK")
    mongo += convertToMongoDbObject(address)

    dbAccess.getOne[Address]("addressId", Identifier(1234)).get should be(Address(Identifier(1234), "addr", "Long Addr", "UK"))
  }

  test("Access to Address that does not exist") {
    val id = Identifier(4321)
    val access = dbAccess

    access.getOne("addressId", id) should be(None)

    access.getAll[Address]("addressId") should be('empty)
  }

  test("Access Multiple Addresses with Single Address in DB") {
    val address1 = Address(Identifier(1234), "addr", "Long Addr", "UK")
    mongo += convertToMongoDbObject(address1)

    dbAccess.getAll[Address]("addressId") should be(List(address1))
  }

  test("Access Multiple Addresses with Two Addresses in DB") {
    val address1 = Address(Identifier(1234), "addr", "Long Addr", "UK")
    val address2 = Address(Identifier(5678), "addr2", "Long Addr 2", "USA")

    mongo += convertToMongoDbObject(address1)
    mongo += convertToMongoDbObject(address2)
    dbAccess.getAll[Address]("addressId") should (contain(address1) and
      contain(address2) and
      have size (2))
  }

  // Part Tests
  test("Access to Part By Id") {
    val vehicle = Some(Vehicle(Identifier(2346), "TestVehicle"))
    val part = Part(Identifier(2345), "woggle sprocket", 1.20, vehicle)
    mongo += convertToMongoDbObject(part)

    dbAccess.getOne[Part]("partId", Identifier(2345)).get should be(Part(Identifier(2345), "woggle sprocket", 1.20, vehicle))
  }

  test("Access to Part that does not exist") {
    val id = Identifier(5432)
    val access = dbAccess

    access.getOne("partId", id) should be(None)

    access.getAll[Part]("partId") should be('empty)
  }

  test("Access Multiple Parts with Single Part in DB") {
    val vehicle1 = Vehicle(Identifier(2347), "TestVehicle")
    val part1 = Part(Identifier(2348), "woggle sprocket", 1.20, Some(vehicle1))
    mongo += convertToMongoDbObject(part1)

    dbAccess.getAll[Part]("partId") should be(List(part1))
  }

  test("Access Multiple Parts with Two Parts in DB") {
    val vehicle1 = Vehicle(Identifier(2349), "TestVehicle1")
    val vehicle2 = Vehicle(Identifier(2350), "TestVehicle2")
    val part1 = Part(Identifier(2351), "woggle sprocket", 1.20, Some(vehicle1))
    val part2 = Part(Identifier(2352), "big woggle sprocket", 1.60, Some(vehicle2))

    mongo += convertToMongoDbObject(part1)
    mongo += convertToMongoDbObject(part2)
    dbAccess.getAll[Part]("partId") should (contain(part1) and
      contain(part2) and
      have size (2))
  }
}