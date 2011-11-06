/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import org.scalatest.matchers.ShouldMatchers

import com.mongodb.casbah.Imports._

import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.id.Identifier
import uk.co.randomcoding.partsdb.db.mongo.MongoConverters._

/**
 * Tests for the Modify functionality of [[uk.co.randomcoding.partsdb.db.mongo.MongoUpdateAccess]]
 *
 * This should include a test for each of the primary types of object.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
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

  test("Modify of Address already added to database correctly modifies object") {
    val address1 = Address(Identifier(9876), "Addr1", "Long Addr 1", "UK")
    mongoAccess add address1 should be(true)

    val address2 = Address(Identifier(9876), "Addr1", "Long Addr 1 Modified", "UK")
    mongoAccess modify address2 should be(true)

    findInDatabase[Address]("addressId", 9876) should be(address2)
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

    findInDatabase[Address]("addressId", 9876) should be(address4)
  }

  test("Modify called on Address that is not is database does not add it to database") {
    val address1 = Address(Identifier(9876), "Addr1", "Long Addr 1", "UK")
    mongoAccess add address1 should be(true)

    val address2 = Address(Identifier(98765), "Addr1", "Long Addr 1", "UK")
    mongoAccess modify address2 should be(false)

    findInDatabase[Address]("addressId", 9876) should be(address1)
    findInDatabase[Address]("addressId", 98765) should be(None)
  }

}