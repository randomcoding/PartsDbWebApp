/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import org.scalatest.matchers.ShouldMatchers
import com.mongodb.casbah.Imports._
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
 */
class MongoUpdateAccessModifyTest extends MongoDbTestBase with ShouldMatchers {

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

  // Part Tests
  test("Modify of Part already added to database correctly modifies object") {
    val part1 = Part(Identifier(9877), "Exhaust", 98.76, Vehicle(Identifier(212), "Vehicle212"))
    mongoAccess add part1 should be(true)

    val part2 = Part(Identifier(9877), "Big Exhaust", 98.76, Vehicle(Identifier(212), "Vehicle212"))
    mongoAccess modify part2 should be(true)

    findInDatabase[Part]("partId", 9877) should be(List(part2))
  }

  test("Multiple modifications to the same Part result in the correct Part in the database") {
    val part1 = Part(Identifier(9878), "Exhaust", 98.76, Vehicle(Identifier(212), "Vehicle212"))
    mongoAccess add part1 should be(true)

    val part2 = Part(Identifier(9878), "Big Exhaust", 98.76, Vehicle(Identifier(212), "Vehicle212"))
    mongoAccess modify part2 should be(true)

    val part3 = Part(Identifier(9878), "Really Big Exhaust", 98.76, Vehicle(Identifier(212), "Vehicle212"))
    mongoAccess modify part3 should be(true)

    val part4 = Part(Identifier(9878), "Enormous Exhaust", 198.76, Vehicle(Identifier(213), "Vehicle213"))
    mongoAccess modify part4 should be(true)

    findInDatabase[Part]("partId", 9878) should be(List(part4))
  }

  test("Modify called on Part that is not is database does not add it to database") {
    val part1 = Part(Identifier(9879), "Exhaust", 98.76, Vehicle(Identifier(213), "Vehicle213"))
    mongoAccess add part1 should be(true)

    val part2 = Part(Identifier(98765), "Enormous Exhaust", 198.76, Vehicle(Identifier(213), "Vehicle213"))
    mongoAccess modify part2 should be(false)

    findInDatabase[Part]("partId", 9879) should be(List(part1))
    findInDatabase[Part]("partId", 98765) should be(Nil)
  }

}