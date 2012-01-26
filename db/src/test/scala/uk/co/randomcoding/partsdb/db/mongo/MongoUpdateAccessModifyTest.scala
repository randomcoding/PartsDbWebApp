/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import org.scalatest.matchers.ShouldMatchers
import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.id.Identifier
import uk.co.randomcoding.partsdb.db.mongo.MongoConverters._
import uk.co.randomcoding.partsdb.core.part.Part
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle

/**
 * Tests for the Modify functionality of [[uk.co.randomcoding.partsdb.db.mongo.MongoUpdateAccess]]
 *
 * This should include a test for each of the primary types of object.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 * @author Jane Rowe
 *
 * @deprecated("Changing Access API")
 */
class MongoUpdateAccessModifyTest extends MongoDbTestBase with ShouldMatchers {
  /*
  val dbName = "UpdateTest"

  lazy val mongoAccess = new MongoUpdateAccess() {
    override val collection = mongo
  }

  private def findInDatabase[T](idFieldName: String, id: Long)(implicit mf: Manifest[T]): List[T] = {
    mongo.find(MongoDBObject(idFieldName -> MongoDBObject("id" -> id))).toList map (convertFromMongoDbObject[T](_))
  }

  // Address Tests
  test("Modify of Address already added to database correctly modifies object") {
    val address1 = Address(Identifier(9876), "Addr1", "Long Addr 1", "UK")
    mongoAccess add address1 should be(true)

    val address2 = Address(Identifier(9876), "Addr1", "Long Addr 1 Modified", "UK")
    mongoAccess modify address2 should be(true)

    findInDatabase[Address]("addressId", 9876) should be(List(address2))
  }

  test("Multiple modifications to the same Address result in the correct Address in the database") {
    val address1 = Address(Identifier(9876), "Addr1", "Long Addr 1", "UK")
    mongoAccess add address1 should be(true)

    val address2 = Address(Identifier(9876), "Addr1", "Long Addr 1 Modified", "UK")
    mongoAccess modify address2 should be(true)

    val address3 = Address(Identifier(9876), "Addr2", "Long Addr 1 Modified", "UK")
    mongoAccess modify address3 should be(true)

    val address4 = Address(Identifier(9876), "Addr2", "Long Addr 2 Modified", "USA")
    mongoAccess modify address4 should be(true)

    findInDatabase[Address]("addressId", 9876) should be(List(address4))
  }

  test("Modify called on Address that is not is database does not add it to database") {
    val address1 = Address(Identifier(9876), "Addr1", "Long Addr 1", "UK")
    mongoAccess add address1 should be(true)

    val address2 = Address(Identifier(98765), "Addr1", "Long Addr 1", "UK")
    mongoAccess modify address2 should be(false)

    findInDatabase[Address]("addressId", 9876) should be(List(address1))
    findInDatabase[Address]("addressId", 98765) should be(Nil)
  }

  // Part Tests with no options
  test("Modify of Part already added to database correctly modifies object") {
    val part1 = Part(Identifier(9877), "Exhaust")
    mongoAccess add part1 should be(true)

    val part2 = Part(Identifier(9877), "Big Exhaust")
    mongoAccess modify part2 should be(true)

    findInDatabase[Part]("partId", 9877) should be(List(part2))
  }

  test("Multiple modifications to the same Part result in the correct Part in the database") {
    val part1 = Part(Identifier(9878), "Exhaust")
    mongoAccess add part1 should be(true)

    val part2 = Part(Identifier(9878), "Big Exhaust")
    mongoAccess modify part2 should be(true)

    val part3 = Part(Identifier(9878), "Really Big Exhaust")
    mongoAccess modify part3 should be(true)

    val part4 = Part(Identifier(9878), "Enormous Exhaust")
    mongoAccess modify part4 should be(true)

    findInDatabase[Part]("partId", 9878) should be(List(part4))
  }

  test("Modify called on Part that is not is database does not add it to database") {
    val part1 = Part(Identifier(9879), "Exhaust")
    mongoAccess add part1 should be(true)

    val part2 = Part(Identifier(98765), "Enormous Exhaust")
    mongoAccess modify part2 should be(false)

    findInDatabase[Part]("partId", 9879) should be(List(part1))
    findInDatabase[Part]("partId", 98765) should be(Nil)
  }

  // Vehicle Tests
  test("Modify of Vehicle already added to database correctly modifies object") {
    val vehicle1 = Vehicle(Identifier(9880), "Vehicle 9880")
    mongoAccess add vehicle1 should be(true)

    val vehicle2 = Vehicle(Identifier(9880), "Vehicle 9880 Modified")
    mongoAccess modify vehicle2 should be(true)

    findInDatabase[Vehicle]("vehicleId", 9880) should be(List(vehicle2))
  }

  test("Multiple modifications to the same Vehicle result in the Vehicle Part in the database") {
    val vehicle1 = Vehicle(Identifier(9881), "Vehicle 9881")
    mongoAccess add vehicle1 should be(true)

    val vehicle2 = Vehicle(Identifier(9881), "Big Vehicle 9881")
    mongoAccess modify vehicle2 should be(true)

    val vehicle3 = Vehicle(Identifier(9881), "Really Big Vehicle 9881")
    mongoAccess modify vehicle3 should be(true)

    val vehicle4 = Vehicle(Identifier(9881), "Enormous Vehicle 9881")
    mongoAccess modify vehicle4 should be(true)

    findInDatabase[Vehicle]("vehicleId", 9881) should be(List(vehicle4))
  }

  test("Modify called on Vehicle that is not is database does not add it to database") {
    val vehicle1 = Vehicle(Identifier(9882), "Vehicle 9882")
    mongoAccess add vehicle1 should be(true)

    val vehicle2 = Vehicle(Identifier(98822), "Enormous Vehicle 98822")
    mongoAccess modify vehicle2 should be(false)

    findInDatabase[Vehicle]("vehicleId", 9882) should be(List(vehicle1))
    findInDatabase[Vehicle]("vehicleId", 98822) should be(Nil)
  }*/

}