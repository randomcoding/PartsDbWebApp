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

    add("Part 1", vehicle, Some("mod123")) should be(part)

    (Part where (_.partName eqs "Part 1") fetch) should be(List(Part.createRecord.partName("Part 1").vehicle(vehicle.id.get).modId(Some("mod123"))))
  }

  test("Adding parts with the same name, but different other properties adds the part if the vehicle is different but does not if it is the same") {
    val vehicle1 = Vehicle.createRecord.vehicleName("v1")
    val part = add("Part 1", vehicle1, Some("mod123"))
    part should be('defined)
    val vehicle2 = Vehicle.createRecord.vehicleName("v2")
    val part2 = add("Part 1", vehicle2, Some("mod123"))
    part2 should be(Some(Part.create("Part 1", vehicle2, Some("mod123"))))
    add("Part 1", vehicle1, Some("mod456")) should be(part)
    add("Part 1", vehicle2, Some("mod456")) should be(part2)

    (Part where (_.partName eqs "Part 1") fetch) should (have size (2) and
      contain(Part.create("Part 1", vehicle1, Some("mod123"))) and
      contain(Part.create("Part 1", vehicle2, Some("mod123"))))
  }

  test("Find Matching parts") {
    val vehicle1 = Vehicle.createRecord.vehicleName("v1")
    val vehicle2 = Vehicle.createRecord.vehicleName("v2")
    val part = add("Part 1", vehicle1, Some("Mod1")).get

    (Part where (_.partName eqs "Part 1") get) should be(Some(part))
    (Part where (_.vehicle eqs vehicle1.id.get) get) should be(Some(part))
    (Part where (_.partName eqs "Part 1") and (_.vehicle eqs vehicle1.id.get) get) should be(Some(part))
    (Part where (_.partName eqs "Part 1") and (_.vehicle eqs vehicle1.id.get) fetch) should be(List(part))

    val part2 = Part.create("Part 1-1", vehicle1, Some("Mod1-1")).id(part.id.get)
    findMatching(part2) should be(Some(part))

    val part3 = Part.create("Part 1", vehicle1, Some("Mod1-1"))
    findMatching(part3) should be(Some(part))

    val part4 = Part.create("Part 1-2", vehicle1, Some("Mod1"))
    findMatching(part4) should be(None)

    val part5 = Part.create("Part 1", vehicle2, Some("Mod1"))
    findMatching(part5) should be(None)
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