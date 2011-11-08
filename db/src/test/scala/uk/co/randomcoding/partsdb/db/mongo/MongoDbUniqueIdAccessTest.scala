/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import org.scalatest.matchers.ShouldMatchers
import com.mongodb.casbah.Imports._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
class MongoDbUniqueIdAccessTest extends MongoDbTestBase with ShouldMatchers {
  override val dbName = "accessIdTest"

  val uniqueIdField = "uniqueId"

  lazy val access = new MongoIdentifierAccess() {
    override val collection = mongo
  }

  test("Call to nextUniqueId returns 0 when the collection contains no entry for unique id") {
    val query = uniqueIdField $exists true
    mongo.find(query).isEmpty should be(true)
    access.nextId() should be(0)
  }

  test("Call to nextUniqueId updates the database with the next value") {
    access.nextId() should be(0)
    val query = uniqueIdField $exists true
    val result = mongo.findOne(query).get
    result.as[Long](uniqueIdField) should be(1)
  }

  test("Sequential call to nextUniqeId return sequential values") {
    access.nextId() should be(0)
    access.nextId() should be(1)
    access.nextId() should be(2)
    access.nextId() should be(3)
    access.nextId() should be(4)
    access.nextId() should be(5)
  }

  test("Multiple calls to nextUniqueId return distinct values") {
    val testCount = 1000

    (0 to testCount) foreach { index => access.nextId() should be(index) }
  }
}