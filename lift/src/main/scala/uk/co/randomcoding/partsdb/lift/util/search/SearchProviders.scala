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
  private val searchProviders: MSet[SearchPageProvider] = MSet.empty

  /**
   * Register a [[uk.co.randomcoding.partsdb.db.search.SearchProvider]] for use with the WebApp
   */
  def register(provider: SearchPageProvider): Unit = searchProviders += provider

  /**
   * Get the registered search providers, sorted by their name
   */
  def providers = searchProviders.toList sortBy (_.searchProvider.name)

  def providerFor(providesType: String) = searchProviders.find(_.searchProvider.providesType == providesType)
}