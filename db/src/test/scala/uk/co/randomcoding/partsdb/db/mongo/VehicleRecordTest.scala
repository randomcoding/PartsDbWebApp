/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import com.foursquare.rogue.Rogue._
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class VehicleRecordTest extends MongoDbTestBase {

  override val dbName = "VehicleRecordTest"

  test("Adding a single vehicle works ok") {
    val v = add("vehicle1")
    v should be('defined)

    v.get.vehicleName.get should be("vehicle1")
  }

  test("Adding the same vehicle more than once does not result in a duplication, or a change of underlying id") {
    val v1 = add("Vehicle 2").get
    val v2 = add("Vehicle 2")

    v1.vehicleName.get should be("Vehicle 2")
    v2 should be(None)

    val namedVehicles = Vehicle where (_.vehicleName eqs "Vehicle 2") fetch

    namedVehicles should have size (1)
    namedVehicles(0).vehicleName.get should be("Vehicle 2")

    val allVehicles = Vehicle where (_.id exists true) fetch

    allVehicles should have size (1)
    allVehicles(0).vehicleName.get should be("Vehicle 2")
  }

  test("Removing a vehicle") {
    add("V2") should be('defined)
    add("V3") should be('defined)

    remove("V2") should be(List(true))

    val allVehicles = Vehicle where (_.id exists true) fetch

    allVehicles should have size (1)

    allVehicles(0).vehicleName.get should be("V3")
  }

  test("Removing a vehicle from an empty database") {
    remove("V1") should be('empty)
  }

  test("Find a named vehicle that is present in the database") {
    val v1 = add("V1")
    val v2 = findNamed("V1")

    v2 should be(List(v1.get))
  }

  test("Find a named vehicle that is not present in the database") {
    add("v123")
    findNamed("v456") should be(Nil)
  }

  test("Rename a vehicle") {
    add("V678") should be('defined)
    rename("V678", "V987")

    findNamed("V678") should be(Nil)
    findNamed("V987") should be(List(Vehicle.createRecord.vehicleName("V987")))
    findNamed("V987")(0).vehicleName.get should be("V987")

    (Vehicle where (_.vehicleName eqs "V987") fetch) should be(List(Vehicle.createRecord.vehicleName("V987")))
  }
}