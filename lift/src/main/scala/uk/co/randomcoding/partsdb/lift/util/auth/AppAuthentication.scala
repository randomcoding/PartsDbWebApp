/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.auth

import net.liftweb.http.auth.HttpDigestAuthentication
import net.liftweb.http.auth.HttpBasicAuthentication
import net.liftweb.http.auth.userRoles
import net.liftweb.common.Loggable
import net.liftweb.http.auth.AuthRole

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object AppAuthentication extends Loggable {
  lazy val simpleAuth = HttpBasicAuthentication("AM2") {
    // TODO: Add user access code here
    case ("Am2User", "Am2aM2", req) => {
      logger.info("You are now authenticated !")
      userRoles(AuthRole("user"))
      true
    }
    case ("Am2Admin", "Am2AdM1n", req) => {
      logger.info("Admin Authenticated")
      userRoles(AuthRole("admin"))
      true
    }
  }

  /*lazy val digestAuth = HttpDigestAuthentication("Am2 App") {
	  val (user, req, auth) => {
	    
	  }
  }*/
}