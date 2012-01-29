/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.auth

import uk.co.randomcoding.partsdb.db.mongo.MongoUserAccess
import uk.co.randomcoding.partsdb.db.mongo.MongoConversionFormats
import uk.co.randomcoding.partsdb.db.util.Helpers._
import net.liftweb.common.Loggable
import net.liftweb.http.auth.{ userRoles, HttpBasicAuthentication, AuthRole }
import uk.co.randomcoding.partsdb.lift.model.Session
import uk.co.randomcoding.partsdb.core.user.Role

/**
 * Authentication mechanisms for use in `Boot.scala`
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object AppAuthentication extends Loggable with MongoConversionFormats {
  private lazy val dbAccess = MongoUserAccess()
  import dbAccess._

  /**
   * Provides Http Basic authentication against the user database
   *
   * == Example ==
   *
   * To use simply add
   * {{{
   * LiftRules.authentication = AppAuthentication.simpleAuth
   * }}}
   * to `bootstrap.liftweb.Boot.scala`
   */
  lazy val simpleAuth = HttpBasicAuthentication("AM2") {
    case ("Am2User", "Am2aM2", req) => loginUser("Am2User", "User")
    case ("Am2Admin", "Am2AdM1n", req) => loginUser("Am2Admin", "Admin")
    case (user, pass, _) => {
      authenticateUser(user, hash(pass)) match {
        case Some(role) => loginUser(user, role)
        case _ => loginFailed(user, pass)
      }
    }
  }

  private[this] def loginFailed(userName: String, pass: String) = {
    logger.error("Failed to Authenticate user %s using password %s".format(userName, pass))
    false
  }

  private[this] def loginUser(userName: String, userRole: Role.Role) = {
    logger.info("User %s authenticated into role: %s".format(userName, userRole))
    userRoles(AuthRole(userRole.toString))
    // TODO: This does not yet work, It would be nice if it did (see LoggedInAs.scala for usage)
    Session.currentUser.set(userName, userRole)
    logger.debug("Current User from session is now: %s".format(Session.currentUser.is))
    true
  }

}