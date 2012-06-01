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
  private[this] val defaultPdfFile = "pdfFile.pdf"

  private[this] def vehicle(name: String, pdfFile: String = defaultPdfFile) = Vehicle(name, pdfFile)

  test("Adding a single vehicle works ok") {
    val v = add(vehicle("vehicle1"))
    v should be('defined)

    v.get.vehicleName.get should be("vehicle1")
  }

  test("Adding the same vehicle more than once does not result in a duplication, or a change of underlying id") {
    val v1 = add(vehicle("Vehicle 2")).get
    val v2 = add(vehicle("Vehicle 2"))

    v1.vehicleName.get should be("Vehicle 2")
    v2 should be(Some(v1))

    val namedVehicles = Vehicle where (_.vehicleName eqs "Vehicle 2") fetch

    namedVehicles should have size (1)
    namedVehicles(0).vehicleName.get should be("Vehicle 2")

    val allVehicles = Vehicle fetch

    allVehicles should have size (1)
    allVehicles(0).vehicleName.get should be("Vehicle 2")
  }

  test("Removing a vehicle") {
    add(vehicle("V2")) should be('defined)
    add(vehicle("V3")) should be('defined)

    remove("V2") should be(List(true))

    val allVehicles = Vehicle fetch

    allVehicles should have size (1)

    allVehicles(0).vehicleName.get should be("V3")
  }

  test("Removing a vehicle from an empty database") {
    remove("V1") should be('empty)
  }

  test("Find a named vehicle that is present in the database") {
    val v1 = add(vehicle("V1"))
    val v2 = findNamed("V1")

    v2 should be(List(v1.get))
  }

  test("Find a named vehicle that is not present in the database") {
    add(vehicle("v123"))
    findNamed("v456") should be(Nil)
  }

  test("Rename a vehicle") {
    add(vehicle("V678")) should be('defined)

    rename("V678", "V987")

    findNamed("V678") should be(Nil)
    findNamed("V987") should be(List(Vehicle.createRecord.vehicleName("V987")))
    findNamed("V987")(0).vehicleName.get should be("V987")

    (Vehicle where (_.vehicleName eqs "V987") fetch) should be(List(Vehicle.createRecord.vehicleName("V987")))
  }

  test("Rename a vehicle does not modify its object id") {
    val v1 = add(vehicle("V678"))
    v1 should be('defined)

    val oid = v1.get.id.get

    val v2 = rename("V678", "V987")

    v2 should be('defined)

    findNamed("V987")(0).id.get should be(oid)

    v2.get.vehicleName.get should be("V987")
  }
}