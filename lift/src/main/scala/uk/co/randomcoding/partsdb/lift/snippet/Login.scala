/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import uk.co.randomcoding.partsdb.core.user.Role.{USER, NO_ROLE, ADMIN}
import uk.co.randomcoding.partsdb.db.mongo.MongoUserAccess._
import uk.co.randomcoding.partsdb.db.util.Helpers.{hash => pwhash}
import uk.co.randomcoding.partsdb.lift.model.Session
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._

import net.liftweb.common.StringOrNodeSeq.strTo
import net.liftweb.common.Logger
import net.liftweb.http.SHtml._
import net.liftweb.http.S
import net.liftweb.util.Helpers._

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
