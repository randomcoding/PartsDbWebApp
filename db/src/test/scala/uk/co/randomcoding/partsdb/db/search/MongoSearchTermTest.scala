/**
 *
 */
package uk.co.randomcoding.partsdb.db.search

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import com.mongodb.casbah.Imports._
import uk.co.randomcoding.partsdb.db.search.SearchTerm._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class MongoSearchTermTest extends FunSuite with ShouldMatchers {
  test("Search Term creates correct query for exists") {
    MongoSearchTerm("key", exists).query should be(("key" $exists true))
  }

  test("Search Term creates correct query for does not exist") {
    MongoSearchTerm("key", doesNotExist).query should be(("key" $exists false))
  }

  test("Search Term creates correct query for a general string query") {
    MongoSearchTerm("key", "value").query should be(MongoDBObject("key" -> "value"))
  }

}