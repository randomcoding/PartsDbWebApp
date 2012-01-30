/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import com.foursquare.rogue.Rogue._

import uk.co.randomcoding.partsdb.core._
import vehicle.Vehicle
import part.Part
import part.Part._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class PartRecordTest extends MongoDbTestBase {
  override val dbName = "PartRecordTest"

  test("Add new part") {
    val vehicle = Vehicle.createRecord.vehicleName("v1")
    val part = add("Part 1", vehicle, Some("mod123"))
    part should be('defined)
    part.get.partName.get should be("Part 1")
    part.get.vehicle.get should be(vehicle.id.get)
    part.get.modId.get should be(Some("mod123"))
  }

  test("Adding the same part multiple times does not create duplicate entries in the database") {
    val vehicle = Vehicle.createRecord.vehicleName("v1")
    val part = add("Part 1", vehicle, Some("mod123"))
    part should be('defined)

    add("Part 1", vehicle, Some("mod123")) should be('empty)

    (Part where (_.partName eqs "Part 1") fetch) should be(List(Part.createRecord.partName("Part 1").vehicle(vehicle.id.get).modId(Some("mod123"))))
  }

  test("Adding parts with the same name, but different other properties does not create additional entries with the same part name in the database") {
    val vehicle1 = Vehicle.createRecord.vehicleName("v1")
    val part = add("Part 1", vehicle1, Some("mod123"))
    part should be('defined)
    val vehicle2 = Vehicle.createRecord.vehicleName("v2")
    add("Part 1", vehicle2, Some("mod123")) should be('empty)
    add("Part 1", vehicle1, Some("mod456")) should be('empty)
    add("Part 1", vehicle2, Some("mod456")) should be('empty)

    (Part where (_.partName eqs "Part 1") fetch) should be(List(Part.createRecord.partName("Part 1").vehicle(vehicle1.id.get).modId(Some("mod123"))))
  }

  test("Remove an existing part") {
    val vehicle1 = Vehicle.createRecord.vehicleName("v1")
    val vehicle2 = Vehicle.createRecord.vehicleName("v2")
    val part1 = add("Part 1", vehicle1, Some("mod123"))
    val part2 = add("Part 2", vehicle1, Some("mod456"))

    remove("Part 1") should be(List(true))

    (Part where (_.id exists true) fetch) should be(List(part2.get))
  }

  test("Remove a part that is not present in a populated database") {
    val vehicle1 = Vehicle.createRecord.vehicleName("v1")
    val vehicle2 = Vehicle.createRecord.vehicleName("v2")
    val part1 = add("Part 1", vehicle1, Some("mod123"))
    val part2 = add("Part 2", vehicle1, Some("mod456"))

    remove("Part 3") should be(Nil)
  }

  test("Remove a part from an empty database") {
    remove("Part 123") should be(Nil)
  }

  test("Find a part by name that is present in the database") {
    val vehicle1 = Vehicle.createRecord.vehicleName("v1")
    val vehicle2 = Vehicle.createRecord.vehicleName("v2")
    val part1 = add("Part 1", vehicle1, Some("mod123"))
    val part2 = add("Part 2", vehicle1, Some("mod456"))

    findNamed("Part 1") should be(List(part1.get))
    findNamed("Part 2") should be(List(part2.get))
  }

  test("Find a part by name that is not present in the populated database") {
    val vehicle1 = Vehicle.createRecord.vehicleName("v1")
    val vehicle2 = Vehicle.createRecord.vehicleName("v2")
    val part1 = add("Part 1", vehicle1, Some("mod123"))
    val part2 = add("Part 2", vehicle1, Some("mod456"))
    findNamed("Part 3") should be(Nil)
  }

  test("Find a part in an empty database") {
    findNamed("Part 3") should be(Nil)
  }

  test("Modify a part that is in the database") {
    val vehicle1 = Vehicle.createRecord.vehicleName("v1")
    val vehicle2 = Vehicle.createRecord.vehicleName("v2")
    val part1 = add("Part 1", vehicle1, Some("mod123"))
    modify("Part 1", "Part 1-1", vehicle2, Some("mod987"))

    findNamed("Part 1") should be(Nil)
    findNamed("Part 1-1") should be(List(Part.createRecord.partName("Part 1-1").vehicle(vehicle2.id.get).modId("mod987")))
  }

  test("Modify a part that is not in the database") {
    val vehicle1 = Vehicle.createRecord.vehicleName("v1")
    modify("Part 3", "Part 3-1", vehicle1, Some("mod987")) should be(())
  }

}