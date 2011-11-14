/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import com.mongodb.casbah.Imports._
import org.scalatest.{ FunSuite, BeforeAndAfterEach }
import org.scalatest.matchers.ShouldMatchers

/**
 * A base class for all tests involving MongoDB usage.
 *
 * This creates a collection called `mongo` which uses the `dbName` and `collectionName` variables which the test class is required to define.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
abstract class MongoDbTestBase extends FunSuite with BeforeAndAfterEach with ShouldMatchers {
  /**
   * Define the database name to use for testing
   */
  val dbName: String

  /**
   * Define the collection name to use for testing
   */
  lazy val collectionName: String = dbName + "Collection"

  /**
   * The MongoDB Collection instance that is to be used for all tests
   */
  var mongo: MongoCollection = _

  override def beforeEach(): Unit = {
    dbName should not equal collectionName
    mongo = MongoConfig.getCollection(dbName, collectionName)
  }

  override def afterEach(): Unit = {
    val mdb = MongoConnection()
    mdb.dropDatabase(dbName)

    mdb.dbNames.toList should not contain (dbName)
  }
}