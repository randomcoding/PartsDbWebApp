/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import scala.xml.{Text, NodeSeq}

import uk.co.randomcoding.partsdb.core.user.Role.{stringToRole, Role, NO_ROLE}
import uk.co.randomcoding.partsdb.lift.model.Session

import net.liftweb.common.Logger
import net.liftweb.http.SHtml
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
    val logoutLink = SHtml.link("/logout", () => Session.currentUser(("", "")), Text("logout"))

    <span>{ loggedInText } { logoutLink }</span>
  }
}
