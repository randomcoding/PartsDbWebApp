/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import org.scalatest.matchers.ShouldMatchers
import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.id.Identifier
import com.mongodb.casbah.Imports._

import uk.co.randomcoding.partsdb.db.mongo.MongoConverters._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
class MongoUpdateAccessTest extends MongoDbTestBase with ShouldMatchers {
  val dbName = "UpdateTest"

  lazy val mongoAccess = new MongoUpdateAccess() {
    override val collection = mongo
  }

  test("Adding an Address") {
    val address = Address(Identifier(3579), "Short", "Long", "UK")

    mongoAccess add address

    val result = mongo.find(MongoDBObject("addressId" -> MongoDBObject("id" -> 3579))).toList map (convertFromMongoDbObject[Address](_))

    result.toList should be(List(address))
  }

  test("Updating an Address with new details works through the method") {
    val address1 = Address(Identifier(3579), "Short", "Long", "UK")
    val address2 = Address(Identifier(3579), "Short", "Long Again", "UK")

    mongoAccess add address1
    mongoAccess add address2

    val result = mongo.find(MongoDBObject("addressId" -> MongoDBObject("id" -> 3579))).toList map (convertFromMongoDbObject[Address](_))

    result.toList should be(List(address2))
  }

  test("Adding an Identifier") {
    fail("Not Implemented Yet")
  }

  // TODO: Add tests for all other major types
}