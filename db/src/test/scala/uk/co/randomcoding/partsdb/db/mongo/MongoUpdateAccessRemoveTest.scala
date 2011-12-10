/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import com.mongodb.casbah.Imports._
import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.id.Identifier
import uk.co.randomcoding.partsdb.db.mongo.MongoConverters._
import uk.co.randomcoding.partsdb.core.part.Part

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 * @author Jane Rowe
 */
class MongoUpdateAccessRemoveTest extends MongoDbTestBase {
  val dbName = "AccessRemoveTest"

  lazy val mongoAccess = new MongoUpdateAccess() {
    override val collection = mongo
  }

  // Address Tests
  val findAddress = (id: Long) => {
    mongo.findOne(MongoDBObject("addressId" -> MongoDBObject("id" -> id))) match {
      case None => None
      case Some(dbo) => Some(convertFromMongoDbObject[Address](dbo))
    }
  }

  test("Remove an Address from the database does remove it") {
    val address = Address(Identifier(3579), "Test 1", "Address for Test 1", "UK")

    //val addrDb: DBObject = address

    mongo += address
    findAddress(3579) should be(Some(address))

    mongoAccess.remove(address) should be(true)

    findAddress(3579) should be(None)
  }

  test("Remove an address from an empty database returns false") {
    val address1 = Address(Identifier(3579), "Test 1", "Address for Test 1", "UK")
    val address2 = Address(Identifier(1234), "Test 2", "Address for Test 2", "UK")
    val address3 = Address(Identifier(5421), "Test 3", "Address for Test 3", "UK")
    val address4 = Address(Identifier(6789), "Test 4", "Address for Test 4", "UK")

    findAddress(3579) should be(None)
    findAddress(1234) should be(None)
    findAddress(5421) should be(None)
    findAddress(6789) should be(None)

    mongoAccess remove address1 should be(false)
    mongoAccess remove address2 should be(false)
    mongoAccess remove address3 should be(false)
    mongoAccess remove address4 should be(false)
  }

  test("Remove an address from a database with multiple addresses in removes only the correct address") {
    val address1 = Address(Identifier(3579), "Test 1", "Address for Test 1", "UK")
    val address2 = Address(Identifier(1234), "Test 2", "Address for Test 2", "UK")
    val address3 = Address(Identifier(5421), "Test 3", "Address for Test 3", "UK")
    val address4 = Address(Identifier(6789), "Test 4", "Address for Test 4", "UK")

    mongo += address1
    mongo += address2
    mongo += address3
    mongo += address4

    findAddress(3579) should be(Some(address1))
    findAddress(1234) should be(Some(address2))
    findAddress(5421) should be(Some(address3))
    findAddress(6789) should be(Some(address4))

    mongoAccess remove address1 should be(true)

    findAddress(3579) should be(None)

    mongo.find("addressId" $exists true).toList map (convertFromMongoDbObject[Address](_)) should (
      contain(address2) and
      contain(address3) and
      contain(address4) and
      have size (3))
  }

  test("Remove an address multiple times only removes it once and does not remove any other entries from the database") {
    val address1 = Address(Identifier(3579), "Test 1", "Address for Test 1", "UK")
    val address2 = Address(Identifier(1234), "Test 2", "Address for Test 2", "UK")
    val address3 = Address(Identifier(5421), "Test 3", "Address for Test 3", "UK")
    val address4 = Address(Identifier(6789), "Test 4", "Address for Test 4", "UK")

    mongo += address1
    mongo += address2
    mongo += address3
    mongo += address4

    mongoAccess remove address1 should be(true)
    mongoAccess remove address1 should be(false)
    mongoAccess remove address1 should be(false)
    mongoAccess remove address1 should be(false)

    mongo.find("addressId" $exists true).toList map (convertFromMongoDbObject[Address](_)) should (
      contain(address2) and
      contain(address3) and
      contain(address4) and
      have size (3))
  }

  // Part Tests
  val findPart = (id: Long) => {
    mongo.findOne(MongoDBObject("partId" -> MongoDBObject("id" -> id))) match {
      case None => None
      case Some(dbo) => Some(convertFromMongoDbObject[Part](dbo))
    }
  }

  test("Remove a part from the database does remove it") {
    val part = Part(Identifier(3579), "Part Test 1", 1.11)

    mongo += part
    findPart(3579) should be(Some(part))

    mongoAccess.remove(part) should be(true)

    findPart(3579) should be(None)
  }

  test("Remove a part from an empty database returns false") {
    val part1 = Part(Identifier(3575), "Part Test 1", 1.11)
    val part2 = Part(Identifier(3576), "Part Test 2", 2.22)
    val part3 = Part(Identifier(3577), "Part Test 3", 3.33)
    val part4 = Part(Identifier(3578), "Part Test 4", 4.44)

    findPart(3575) should be(None)
    findPart(3576) should be(None)
    findPart(3577) should be(None)
    findPart(3578) should be(None)

    mongoAccess remove part1 should be(false)
    mongoAccess remove part2 should be(false)
    mongoAccess remove part3 should be(false)
    mongoAccess remove part4 should be(false)
  }

  test("Remove a part from a database with multiple part in removes only the correct part") {
    val part1 = Part(Identifier(3575), "Part Test 1", 1.11)
    val part2 = Part(Identifier(3576), "Part Test 2", 2.22)
    val part3 = Part(Identifier(3577), "Part Test 3", 3.33)
    val part4 = Part(Identifier(3578), "Part Test 4", 4.44)

    mongo += part1
    mongo += part2
    mongo += part3
    mongo += part4

    findPart(3575) should be(Some(part1))
    findPart(3576) should be(Some(part2))
    findPart(3577) should be(Some(part3))
    findPart(3578) should be(Some(part4))

    mongoAccess remove part1 should be(true)

    findPart(3575) should be(None)

    mongo.find("partId" $exists true).toList map (convertFromMongoDbObject[Part](_)) should (
      contain(part2) and
      contain(part3) and
      contain(part4) and
      have size (3))
  }

  test("Remove a part multiple times only removes it once and does not remove any other entries from the database") {
    val part1 = Part(Identifier(3575), "Part Test 1", 1.11)
    val part2 = Part(Identifier(3576), "Part Test 2", 2.22)
    val part3 = Part(Identifier(3577), "Part Test 3", 3.33)
    val part4 = Part(Identifier(3578), "Part Test 4", 4.44)

    mongo += part1
    mongo += part2
    mongo += part3
    mongo += part4

    mongoAccess remove part1 should be(true)
    mongoAccess remove part1 should be(false)
    mongoAccess remove part1 should be(false)
    mongoAccess remove part1 should be(false)

    mongo.find("partId" $exists true).toList map (convertFromMongoDbObject[Part](_)) should (
      contain(part2) and
      contain(part3) and
      contain(part4) and
      have size (3))
  }
}