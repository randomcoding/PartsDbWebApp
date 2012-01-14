/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import org.scalatest.matchers.ShouldMatchers
import com.mongodb.casbah.Imports._
import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.part.Part
import uk.co.randomcoding.partsdb.core.id.Identifier
import uk.co.randomcoding.partsdb.db.mongo.MongoConverters._
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 * @author Jane Rowe
 *
 */
class MongoUpdateAccessAddTest extends MongoDbTestBase with ShouldMatchers {
  val dbName = "UpdateTest"

  lazy val mongoAccess = new MongoUpdateAccess() {
    override val collection = mongo
  }

  private def findInDatabase[T](idFieldName: String, id: Long)(implicit mf: Manifest[T]): List[T] = {
    mongo.find(MongoDBObject(idFieldName -> MongoDBObject("id" -> id))).toList map (convertFromMongoDbObject[T](_))
  }

  test("Adding an Address") {
    val address = Address(Identifier(3579), "Short", "Long", "UK")

    mongoAccess add address should be(true)

    val result = findInDatabase[Address]("addressId", 3579)

    result.toList should be(List(address))
  }

  test("Adding an Address with the same id as an existing one but different details does not update the previous one") {
    val address1 = Address(Identifier(4680), "Short", "Long", "UK")
    val address2 = Address(Identifier(4680), "Short", "Long Again", "UK")

    mongoAccess add address1 should be(true)
    mongoAccess add address2 should be(false)

    val result = findInDatabase[Address]("addressId", 4680)

    result.toList should be(List(address1))
  }

  //----------------
  // Part Tests with no options
  test("Adding a Part") {
    val part = Part(Identifier(2468), "sprocket")

    mongoAccess add part should be(true)

    val result = findInDatabase[Part]("partId", 2468)

    result.toList should be(List(part))
  }

  test("Adding a Part with the same id as an existing one but different details does not update the previous one") {
    val part1 = Part(Identifier(4680), "sprocket")
    val part2 = Part(Identifier(4680), "woggle sprocket")

    mongoAccess add part1 should be(true)
    mongoAccess add part2 should be(false)

    val result = findInDatabase[Part]("partId", 4680)

    result.toList should be(List(part1))
  }

  //----------------
  // Part Tests with options
  //    test("Adding a Part") {
  //    val part = Part(Identifier(2468), "ModPartId", "SupplierPartId", "sprocket", 1.00, Some(Vehicle(Identifier(210), "Vehicle210")))
  //
  //    mongoAccess add part should be(true)
  //
  //    val result = findInDatabase[Part]("partId", 2468)
  //
  //    result.toList should be(List(part))
  //  }
  //
  //  test("Adding a Part with the same id as an existing one but different details does not update the previous one") {
  //    val part1 = Part(Identifier(4680), "ModPartId1", "SupplierPartId1", "sprocket", 1.51, Some(Vehicle(Identifier(211), "Vehicle211")))
  //    val part2 = Part(Identifier(4680), "ModPartId2", "SupplierPartId2", "woggle sprocket", 1.52, Some(Vehicle(Identifier(212), "Vehicle212")))
  //
  //    mongoAccess add part1 should be(true)
  //    mongoAccess add part2 should be(false)
  //
  //    val result = findInDatabase[Part]("partId", 4680)
  //
  //    result.toList should be(List(part1))
  //  }

  //----------------
  test("Adding a Vehicle") {
    val vehicle = Vehicle(Identifier(2469), "Vehicle2469")

    mongoAccess add vehicle should be(true)

    val result = findInDatabase[Vehicle]("vehicleId", 2469)

    result.toList should be(List(vehicle))
  }

  test("Adding a Vehicle with the same id as an existing one but different details does not update the previous one") {
    val vehicle1 = Vehicle(Identifier(2470), "MyVehicle")
    val vehicle2 = Vehicle(Identifier(2470), "MyVehicleAltered")

    mongoAccess add vehicle1 should be(true)
    mongoAccess add vehicle2 should be(false)

    val result = findInDatabase[Vehicle]("vehicleId", 2470)

    result.toList should be(List(vehicle1))
  }
  // TODO: Add tests for all other major types
}