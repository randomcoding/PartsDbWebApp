/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import uk.co.randomcoding.partsdb.lift.util.snippet.DbAccessSnippet
import uk.co.randomcoding.partsdb.lift.util.search.SearchProviders

/**
 * Displays the search options and results.
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object DisplaySearch extends DbAccessSnippet {

  private val providers = SearchProviders.providers

  private val seatchTypes = providers map (provider => (provider.name, provider.providesType))

  def render = {

  }
}