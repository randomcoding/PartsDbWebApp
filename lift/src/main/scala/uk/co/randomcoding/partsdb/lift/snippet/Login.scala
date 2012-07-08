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

import uk.co.randomcoding.partsdb.core.user.Role.{ USER, NO_ROLE, ADMIN }
import uk.co.randomcoding.partsdb.db.mongo.MongoUserAccess._
import uk.co.randomcoding.partsdb.db.util.Helpers.{ hash => pwhash }
import uk.co.randomcoding.partsdb.lift.model.Session
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._

import net.liftweb.common.{ Logger, Full }
import net.liftweb.http.SHtml._
import net.liftweb.http.S
import net.liftweb.util.Helpers._
import net.liftweb.util.Props

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object Login extends Logger {

  def render = {
    var username = ""
    var password = ""

    def processLogin() = {
      authenticateUser(username, pwhash(password)) match {
        case Some(role) if role != NO_ROLE => {
          Session.currentUser.set(username, role)
          S.redirectTo(role match {
            case USER => "/app/"
            case ADMIN => "/admin/"
          })
        }
        case _ => S.redirectTo("/")
      }
    }

    "#username" #> styledText(username, username = _) &
      "#password" #> styledPassword(password, password = _) &
      "#loginButton" #> button("Login", processLogin)
  }
}
