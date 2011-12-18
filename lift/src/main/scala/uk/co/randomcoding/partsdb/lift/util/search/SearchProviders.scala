/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.search

import scala.collection.mutable.{ Set => MSet }

import uk.co.randomcoding.partsdb.db.search.MongoSearchProvider

/**
 * Provides access to all the search providers that are available.
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object SearchProviders {
  private val searchProviders: MSet[MongoSearchProvider[_]] = MSet.empty

  /**
   * Register a [[uk.co.randomcoding.partsdb.db.search.SearchProvider]] for use with the WebApp
   */
  def register(provider: MongoSearchProvider[_]): Unit = searchProviders += provider

  /**
   * Get the registered search providers, sorted by their name
   */
  def providers = searchProviders.toList sortBy (_.name)

  def providerFor(providesType: String) = searchProviders.find(_.providesType == providesType)
}