/**
 *
 */
package uk.co.randomcoding.partsdb.db.search

import uk.co.randomcoding.partsdb.db.mongo.MongoDbTestBase

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddressSearchProviderTest extends MongoDbTestBase {
  override val dbName = "MongoDbSearchProviderTest"

  test("Search against an empty db returns no results") {
    fail("DB Access has changed")
  }

  test("Search for Address with single search term returns correct results with single entry in database") {
    fail("DB Access has changed")
  }

  test("Search for Address with single search term that does not match returns correct results with single entry in database") {
    fail("DB Access has changed")
  }

  test("Search for Address with single search term that matchs single result returns correct results with multiple entries in database") {
    fail("DB Access has changed")
  }

  test("Search for Address with single search term that matchs multiple results returns correct results with multiple entries in database") {
    fail("DB Access has changed")
  }

  test("Search for specific Address using multiple search terms when it is only entry in database") {
    fail("DB Access has changed")
  }

  test("Search for specific Address using multiple search terms when there are multiple partial matches in the database") {
    fail("DB Access has changed")
  }

  test("Search with multiple terms for specific Address that should return no matches when there are multiple partial matches in the database") {
    fail("DB Access has changed")
  }
}