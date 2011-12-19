/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import uk.co.randomcoding.partsdb.lift.util.snippet.DbAccessSnippet
import uk.co.randomcoding.partsdb.lift.util.search.SearchProviders
import uk.co.randomcoding.partsdb.lift.util.search.SearchPageProvider
import net.liftweb.http.RequestVar
import net.liftweb.http.S
import net.liftweb.common.Full
import net.liftweb.common.Empty
import net.liftweb.common.Box
import net.liftweb.util.Helpers._
import net.liftweb.http.SHtml._
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._

/**
 * Displays the search options and results.
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object DisplaySearch extends DbAccessSnippet {

  object currentProvider extends RequestVar[Box[SearchPageProvider]](Empty)

  val providers = (None, "Search for Type") :: (SearchProviders.providers map (provider => (Some(provider), provider.searchProvider.providesType)))

  def provider(prov: Option[SearchPageProvider]) = {
    prov match {
      case Some(p) => currentProvider(Full(p))
      case None => currentProvider(Empty)
    }

    S.redirectTo("/app/search")
  }

  val showAll = () => {}
  def render = {

    "#searchType" #> styledObjectSelect(providers, None, provider _) &
      "#showAll" #> button("Show All", showAll, "disabled" -> (if (currentProvider.isEmpty) "disabled" else ""))
  }
}