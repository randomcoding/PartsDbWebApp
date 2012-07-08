/*
 * Copyright (C) 2012 RandomCoder <randomcoder@randomcoding.co.uk>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *    RandomCoder - initial API and implementation and/or initial documentation
 */

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

  val providers = ("", "Search for Type") :: (SearchProviders.providers map (provider => (provider.providesType, provider.providesType)))

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
