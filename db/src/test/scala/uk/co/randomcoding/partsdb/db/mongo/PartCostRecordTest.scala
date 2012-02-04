/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo
import org.bson.types.ObjectId
import org.joda.time.DateTime.now

import com.foursquare.rogue.Rogue._

import uk.co.randomcoding.partsdb.core.part.PartCost.{ remove, findMatching, findByPart, findById, create, add }
import uk.co.randomcoding.partsdb.core.part.{ PartCost, Part }

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class PartCostRecordTest extends MongoDbTestBase {
  import uk.co.randomcoding.partsdb.core.part.PartCost._
  import uk.co.randomcoding.partsdb.core.part.PartCost
  import com.foursquare.rogue.Rogue._

  override val dbName = "PartCostRecordTest"

  val part1 = Part.createRecord.partName("Part1")
  val part2 = Part.createRecord.partName("Part2")

  test("Equality and HashCode") {
    val nowDate = now
    val pc1 = create(part1, 10.0, nowDate.minusHours(1))
    val pc2 = create(part1, 10.0, nowDate.plusHours(1))
    val pc3 = create(part1, 10.0, nowDate.minusDays(30))

    pc1 should (be(pc2) and be(pc3))
    pc2 should (be(pc1) and be(pc3))
    pc3 should (be(pc1) and be(pc2))

    pc1.hashCode should (be(pc2.hashCode) and be(pc3.hashCode))
  }

  test("Adding a new PartCost works ok") {
    val pc = add(part1, 10.0, now)
    pc should be('defined)

    (PartCost where (_.id exists true) fetch) should be(List(pc.get))
  }

  test("Adding the same Part Cost more than once does not result in a duplication, or a change of underlying id") {
    val nowDate = now
    val pc1 = add(part1, 10.0, nowDate)
    pc1 should be('defined)
    add(part1, 10.0, nowDate) should be(pc1)

    (PartCost where (_.id exists true) fetch) should be(List(create(part1, 10.0, nowDate)))
  }

  test("Find Matching works by Object Id only") {
    val pc = add(part1, 10.0, now)
    findMatching(pc.get) should be(pc)
  }

  test("Find Matching correctly identifies matches based on object content") {
    val pc = add(part1, 10.0, now)

    val pc2 = create(part1, 10.0, now.plusMinutes(10))
    findMatching(pc2) should be(pc)
  }

  test("Removing a PartCost that exists in the database") {
    val pc1 = add(part1, 10.0, now).get
    val pc2 = add(part2, 12.0, now.minusMinutes(10)).get
    remove(pc1.id.get)

    (PartCost where (_.id exists true) fetch) should be(List(pc2))
  }

  test("Removing a Part Cost from an empty database") {
    remove(new ObjectId)
  }

  test("Removing a Part Cost that does not exist from a populated database") {
    val pc1 = add(part1, 10.0, now).get
    val pc2 = add(part2, 12.0, now.minusMinutes(10)).get

    remove(new ObjectId)
  }

  test("Find a Part Cost that is present in the database") {
    val pc1 = add(part1, 10.0, now).get
    val pc2 = add(part2, 12.0, now.minusMinutes(10)).get

    findById(pc1.id.get) should be(Some(pc1))
    findById(pc2.id.get) should be(Some(pc2))

    findByPart(part1) should be(List(pc1))
    findByPart(part2) should be(List(pc2))
  }

  test("Find a ??? that is not present in the database") {
    findById(new ObjectId) should be('empty)
    findByPart(part2) should be('empty)
  }

  test("Modify a Part Cost") {
    pending
  }

  test("Modify a Part Cost does not modify its object id") {
    pending
  }

  // These are only required if this has embedded objects, referenced by Object Id
  test("Adding a new Part Cost adds the Part to the database if it does not already exist records to the database") {
    add(part1, 10.0, now) should be('defined)
    (Part where (_.id exists true) fetch) should be(List(part1))
    add(part2, 12.0, now.minusHours(3)) should be('defined)
    (Part where (_.id exists true) fetch) should (have size (2) and
      contain(part1) and
      contain(part2))
  }

  test("Modifying a Part Cost with different Part ids works correctly, if the new part is already added") {
    pending
  }

  test("Modifying a Part Cost with different Part ids works correctly, if the new part is not present in the databse") {
    pending
  }

  test("Modifying a ObjectRefId field will remove the referenced object if it is no longer used in the database") {
    pending
  }

  test("Modifying a ObjectRefId field will not remove the referenced object if it is still in use in the database") {
    pending
  }

}