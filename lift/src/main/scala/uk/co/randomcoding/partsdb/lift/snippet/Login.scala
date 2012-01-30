/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import net.liftweb.util.Helpers._
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import net.liftweb.http.SHtml._
import net.liftweb.http.S
import uk.co.randomcoding.partsdb._
import db.mongo.MongoUserAccess._
import db.util.Helpers.{ hash => pwhash }
import core.user.Role.Role
import core.user.Role._
import uk.co.randomcoding.partsdb.lift.model.Session
import net.liftweb.common.Logger

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
