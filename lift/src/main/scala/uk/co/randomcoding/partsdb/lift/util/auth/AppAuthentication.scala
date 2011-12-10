/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.auth

import uk.co.randomcoding.partsdb.db.mongo.MongoUserAccess
import uk.co.randomcoding.partsdb.db.mongo.MongoConversionFormats
import uk.co.randomcoding.partsdb.db.util.Helpers._

import net.liftweb.common.Loggable
import net.liftweb.http.auth.{ userRoles, HttpBasicAuthentication, AuthRole }

/**
 * Authentication mechanisms for use in `Boot.scala`
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object AppAuthentication extends Loggable with MongoConversionFormats {
  private lazy val dbAccess = MongoUserAccess()
  import dbAccess._

  /**
   * Provides Http Basic authentication.
   *
   * == Example
   * To use simple add
   * {{{
   * LiftRules.authentication = AppAuthentication.simpleAuth
   * }}}
   * to `bootstrap.liftweb.Boot.scala`
   */
  lazy val simpleAuth = HttpBasicAuthentication("AM2") {
    case ("Am2User", "Am2aM2", req) => {
      logger.info("User Am2User authenticated")
      userRoles(AuthRole("user"))
      true
    }
    case ("Am2Admin", "Am2AdM1n", req) => {
      logger.info("Admin Am2Admin authenticated")
      userRoles(AuthRole("admin"))
      true
    }
    case (user, pass, _) => {
      userRole(user, hash(pass)) match {
        case Some(role) => {
          logger.info("User %s authenticated into role: %s".format(user, role))
          userRoles(AuthRole(role))
          true
        }
        case _ => {
          logger.error("Failed to Authenticate user %s using password %s".format(user, pass))
          false
        }
      }
    }
  }

}