/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet.search

import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.search.SearchProviders

import net.liftweb.common.Box.box2Option
import net.liftweb.common.{ Logger, Full, Empty, Box }
import net.liftweb.http.js.JsCmds
import net.liftweb.http.RequestVar
import net.liftweb.util.AnyVar.whatVarIs
import net.liftweb.util.Helpers._

/**
 * Displays the search options and results.
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class DisplaySearch extends Logger {

  object currentProvider extends RequestVar[Box[String]](Empty)

  val providers = ("", "Search for Type") :: (SearchProviders.providers map (provider => (provider.searchProvider.providesType, provider.searchProvider.providesType)))

  def render = {

    def displaySearchControls = {
      info("Display Search Controls Called")
      JsCmds.Noop
    }

    def redirect = "/app/search?selected=%s".format(if (currentProvider.isDefined) currentProvider.is.get else "None")

    def provider(prov: String) = {
      prov match {
        case "" => currentProvider(Empty)
        case p => currentProvider(Full(p))
      }
      debug("Current Provider is now: %s".format(currentProvider.is))
    }

    debug("Current Provider is: %s".format(currentProvider.is))

    "#searchType" #> styledAjaxSelect(providers, if (currentProvider.isDefined) currentProvider.is.get else "", { prov =>
      provider(prov)
      JsCmds.SetHtml("searchControls", SearchProviders.providerFor(prov) match {
        case Some(p) => p.renderSearchControls
        case _ => <h3>No Provider</h3>
      })
    })
  }
}