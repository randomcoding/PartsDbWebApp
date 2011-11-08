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
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
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

  // TODO: Add tests for all other major types
}