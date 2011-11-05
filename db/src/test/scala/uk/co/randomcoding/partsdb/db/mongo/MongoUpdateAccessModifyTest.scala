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
    fail("Not Implemented Yet")
  }

  test("Multiple modifications to the same Addres result in the correct Address in the database") {
    fail("Not Implemented Yet")
  }

  test("Modify called on Address that is not is database does not add it to database") {
    fail("Not Implemented Yet")
  }

}