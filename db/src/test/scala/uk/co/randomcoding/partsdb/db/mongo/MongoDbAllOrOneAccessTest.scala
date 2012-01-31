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
    val address = Address(Identifier(10), "addr", "Long Addr", "UK")
    mongo += convertToMongoDbObject(address)

    dbAccess.getOne[Address]("addressId", Identifier(10)).get should be(Address(Identifier(10), "addr", "Long Addr", "UK"))
  }

  test("Access to Address that does not exist") {
    val id = Identifier(11)
    val access = dbAccess

    access.getOne("addressId", id) should be(None)

    access.getAll[Address]("addressId") should be('empty)
  }

  test("Access Multiple Addresses with Single Address in DB") {
    val address1 = Address(Identifier(12), "addr", "Long Addr", "UK")
    mongo += convertToMongoDbObject(address1)

    dbAccess.getAll[Address]("addressId") should be(List(address1))
  }

  test("Access Multiple Addresses with Two Addresses in DB") {
    val address1 = Address(Identifier(13), "addr", "Long Addr", "UK")
    val address2 = Address(Identifier(14), "addr2", "Long Addr 2", "USA")

    mongo += convertToMongoDbObject(address1)
    mongo += convertToMongoDbObject(address2)
    dbAccess.getAll[Address]("addressId") should (contain(address1) and
      contain(address2) and
      have size (2))
  }

  // Vehicle Tests with no options
  test("Access to Vehicle By Id") {
    val vehicle = Vehicle(Identifier(20), "Vehicle1", "Manual1")
    mongo += convertToMongoDbObject(vehicle)

    dbAccess.getOne[Vehicle]("vehicleId", Identifier(20)).get should be(Vehicle(Identifier(20), "Vehicle1", "Manual1"))
  }

  test("Access to Vehicle that does not exist") {
    val id = Identifier(21)
    val access = dbAccess

    access.getOne("vehicleId", id) should be(None)

    access.getAll[Part]("vehicleId") should be('empty)
  }

  test("Access Multiple Vehicles with Single Vehicle in DB") {
    val vehicle1 = Vehicle(Identifier(22), "Vehicle1", "Manual1")
    mongo += convertToMongoDbObject(vehicle1)

    dbAccess.getAll[Vehicle]("vehicleId") should be(List(vehicle1))
  }

  test("Access Multiple Vehicles with Two Vehicles in DB") {
    val vehicle1 = Vehicle(Identifier(23), "Vehicle1", "Manual1")
    val vehicle2 = Vehicle(Identifier(24), "Vehicle2", "Manual2")

    mongo += convertToMongoDbObject(vehicle1)
    mongo += convertToMongoDbObject(vehicle2)
    dbAccess.getAll[Vehicle]("vehicleId") should (contain(vehicle1) and
      contain(vehicle2) and
      have size (2))
  }

  // Part Tests with no options
  test("Access to Part By Id") {
    val part = Part(Identifier(30), "Part1")
    mongo += convertToMongoDbObject(part)

    dbAccess.getOne[Part]("partId", Identifier(30)).get should be(Part(Identifier(30), "Part1"))
  }

  test("Access to Part that does not exist") {
    val id = Identifier(31)
    val access = dbAccess

    access.getOne("partId", id) should be(None)

    access.getAll[Part]("partId") should be('empty)
  }

  test("Access Multiple Parts with Single Part in DB") {
    val part1 = Part(Identifier(32), "Part1")
    mongo += convertToMongoDbObject(part1)

    dbAccess.getAll[Part]("partId") should be(List(part1))
  }

  test("Access Multiple Parts with Two Parts in DB") {
    val part1 = Part(Identifier(33), "Part1")
    val part2 = Part(Identifier(34), "Part2")

    mongo += convertToMongoDbObject(part1)
    mongo += convertToMongoDbObject(part2)
    dbAccess.getAll[Part]("partId") should (contain(part1) and
      contain(part2) and
      have size (2))
  }

  // Part Tests with options
  test("Access to Part with Options By Id") {
    val optionListVehicle: Option[List[Vehicle]] = Option(List(Vehicle(Identifier(23), "Vehicle1", "Manual1")))
    val optionModString: Option[String] = Option("ModId1")
    val part = Part(Identifier(35), "Part1", optionListVehicle, optionModString)
    mongo += convertToMongoDbObject(part)

    dbAccess.getOne[Part]("partId", Identifier(35)).get should be(Part(Identifier(35), "Part1", optionListVehicle, optionModString))

  }

  test("Access to Part with Options that does not exist") {
    val id = Identifier(36)
    val access = dbAccess

    access.getOne("partId", id) should be(None)

    access.getAll[Part]("partId") should be('empty)
  }

  test("Access Multiple Part with Options with Single Part with Options in DB") {
    val optionListVehicle: Option[List[Vehicle]] = Option(List(Vehicle(Identifier(23), "Vehicle1", "Manual1")))
    val optionModString: Option[String] = Option("ModId1")
    val part1 = Part(Identifier(35), "Part1", optionListVehicle, optionModString)
    mongo += convertToMongoDbObject(part1)

    dbAccess.getAll[Part]("partId") should be(List(part1))
  }

  test("Access Multiple Part with Options with Two Part with Options in DB") {
    val optionListVehicle1: Option[List[Vehicle]] = Option(List(Vehicle(Identifier(24), "Vehicle1", "Manual1")))
    val optionModString1: Option[String] = Option("ModId1")
    val part1 = Part(Identifier(36), "Part1", optionListVehicle1, optionModString1)

    val optionListVehicle2: Option[List[Vehicle]] = Option(List(Vehicle(Identifier(25), "Vehicle1", "Manual1")))
    val optionModString2: Option[String] = Option("ModId2")
    val part2 = Part(Identifier(37), "Part2", optionListVehicle2, optionModString2)

    mongo += convertToMongoDbObject(part1)
    mongo += convertToMongoDbObject(part2)
    dbAccess.getAll[Part]("partId") should (contain(part1) and
      contain(part2) and
      have size (2))
  }
}