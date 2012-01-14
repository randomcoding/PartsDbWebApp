/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet.search

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
import net.liftweb.common.Logger
import scala.xml.NodeSeq
import net.liftweb.http.js.JsCmds
import scala.xml.Text

/**
 * Displays the search options and results.
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class DisplaySearch extends DbAccessSnippet with Logger {

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