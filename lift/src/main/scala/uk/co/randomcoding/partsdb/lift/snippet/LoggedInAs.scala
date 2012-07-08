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
package uk.co.randomcoding.partsdb.lift.snippet

import scala.xml.{ Text, NodeSeq }

import uk.co.randomcoding.partsdb.core.user.Role.{ stringToRole, Role, NO_ROLE }
import uk.co.randomcoding.partsdb.lift.model.Session
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._

import net.liftweb.common.Logger
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.util.Helpers._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object LoggedInAs extends Logger {
  def render = {
    val currentUser = Session.currentUser.is
    "#loggedInAs" #> (currentUser match {
      case (user: String, role: Role) if (user.nonEmpty && role != NO_ROLE) => loggedIn(user, role)
      case _ => notLoggedIn
    })
  }

  private def notLoggedIn: NodeSeq = <span>{ Text("Not Logged In") }</span>

  private def loggedIn(user: String, role: Role): NodeSeq = {
    val loggedInText = Text("Logged in as: %s (%s) - ".format(user, role))
    val logoutLink = plainLink("logout", "/logout", () => Session.currentUser(("", "")))

    span(loggedInText ++ logoutLink, Noop)
  }
}
