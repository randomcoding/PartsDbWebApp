/**
 *
 */
package uk.co.randomcoding.partsdb.db.search

import com.mongodb.casbah.Imports._

/**
 * Provides access to structured searches (via [[uk.co.randomcoding.partsdb.db.search.SearchTerm]]s) on the underlying datastore.
 *
 * @constructor Create a new search provider
 * @param name The name of the search provider.
 * @param collection The collection that this search provider should use
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
abstract class SearchProvider(val name: String, collection: MongoCollection) {
  /**
   * The type of results that this provider returns from its searches
   */
  type ResultType

  /**
   * A String representation of the type of entity this provider returns.
   *
   * e.g. `Customer` or `Quote`
   */
  val providesType: String

  /**
   * Perform the search and get the results form the datastore
   *
   * @param searchTerms A set of distinct [[uk.co.randomcoding.partsdb.db.search.SearchTerm]]s that will be used to get the results of the search
   */
  def search(searchTerms: Set[SearchTerm]): Set[ResultType]
}
