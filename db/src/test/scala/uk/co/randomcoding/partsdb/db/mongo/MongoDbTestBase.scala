/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import scala.collection.JavaConversions._

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{ FunSuite, BeforeAndAfterEach, BeforeAndAfterAll }

import net.liftweb.mongodb.{ MongoIdentifier, MongoDB, DefaultMongoIdentifier }

/**
 * A base class for all tests involving MongoDB usage.
 *
 * This class will initialise the MongoDB connection before each test is run and drop the database,
 * plus any collections that were created, after each test. This ensures that there are no side
 * effects between individual tests.
 *
 *  To use this class, simply override the `dbName` val (which is required anyway) with the name of the database you want
 *  to use for testing.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
abstract class MongoDbTestBase extends FunSuite with BeforeAndAfterEach with BeforeAndAfterAll with ShouldMatchers {

  /**
   * Define the database name to use for testing
   *
   * This ensures that a distinct database is used for each test and that there are no side effects (or effects on the main database)
   */
  val dbName: String

  /**
   * Setup the test database
   */
  override def beforeEach(): Unit = {
    MongoConfig.init(dbName)
    MongoDB.getDb(DefaultMongoIdentifier) should be('defined)
    Thread.sleep(250)
  }

  /**
   * Drop any data in the test database
   */
  override def afterEach(): Unit = {
    object dbid extends MongoIdentifier { override val jndiName = dbName }
    val db = MongoDB.getDb(DefaultMongoIdentifier).get
    db.getCollectionNames filterNot (_ startsWith "system.") foreach (db.getCollection(_).drop)
    db.dropDatabase()
    Thread.sleep(250)
  }

  override def afterAll = Thread.sleep(500)
}