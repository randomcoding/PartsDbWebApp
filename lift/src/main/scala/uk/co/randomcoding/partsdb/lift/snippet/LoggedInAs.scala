/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import net.liftweb.util.Helpers._
import net.liftweb.http.SHtml
import uk.co.randomcoding.partsdb.lift.model.Session
import scala.xml.Text
import scala.xml.NodeSeq
import net.liftweb.common.Logger

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object LoggedInAs extends Logger {
  def render = {
    val currentUser = Session.currentUser.is
    info("Current User: %s".format(currentUser))
    "#loggedInAs" #> (currentUser match {
      case (user: String, role: String) if (user.nonEmpty && role.nonEmpty) => loggedIn(user, role)
      case _ => notLoggedIn
    })
  }

  private def notLoggedIn: NodeSeq = <span>{ Text("Not Logged In") }</span>

  private def loggedIn(user: String, role: String): NodeSeq = {
    val loggedInText = Text("Logged in as: %s (%s) - ".format(user, role))
    val logoutLink = SHtml.link("/app/", () => Session.currentUser(("", "")), Text("logout"))

    <span>{ loggedInText } { logoutLink }</span>
  }
}