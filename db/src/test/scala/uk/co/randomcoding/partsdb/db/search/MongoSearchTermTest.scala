/**
 *
 */
package uk.co.randomcoding.partsdb.db.search

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import uk.co.randomcoding.partsdb.db.search.SearchTerm._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class MongoSearchTermTest extends FunSuite with ShouldMatchers {
  test("Search Term creates correct query for exists") {
    fail("Search API Changed")
    //StringSearchTerm("key", exists).query should be(("key" $exists true))
  }

  test("Search Term creates correct query for does not exist") {
    fail("Search API Changed")
    //StringSearchTerm("key", doesNotExist).query should be(("key" $exists false))
  }

  test("Search Term creates correct query for a general string query") {
    fail("Search API Changed")
    /*    StringSearchTerm("key", "value").query should be(MongoDBObject("key" -> "value"))

    IntegerSearchTerm("key", 30).query should be(MongoDBObject("key" -> 30))

    DoubleSearchTerm("key", 3.1415).query should be(MongoDBObject("key" -> 3.1415))*/
  }

}