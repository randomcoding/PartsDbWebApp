/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.auth

import net.liftweb.http.auth.HttpDigestAuthentication
import net.liftweb.http.auth.HttpBasicAuthentication
import net.liftweb.http.auth.userRoles
import net.liftweb.common.Loggable
import net.liftweb.json._
import net.liftweb.http.auth.AuthRole
import java.security.MessageDigest
import com.mongodb.casbah.Imports._
import uk.co.randomcoding.partsdb.db.mongo.MongoConfig
import uk.co.randomcoding.partsdb.db.mongo.MongoConversionFormats

/**
 * Authentication mechanisms for use in `Boot.scala`
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object AppAuthentication extends Loggable with MongoConversionFormats {
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
      logger.info("You are now authenticated !")
      userRoles(AuthRole("user"))
      true
    }
    case ("Am2Admin", "Am2AdM1n", req) => {
      logger.info("Admin Authenticated")
      userRoles(AuthRole("admin"))
      true
    }
    case (user, pass, _) => {
      authCollection.findOne(authUserQuery(user, pass)) match {
        case Some(dbo: DBObject) if (dbo.getAs[String]("userRole") isDefined) => {
          val role = dbo.getAs[String]("usercommonRole").get
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

  // TODO: This needs to move into the db project
  def addUser(userName: String, password: String, userRole: String) = {
    authCollection.findOne(findUser(userName)) match {
      case Some(dbo) => logger.error("User %s already exists. Please use modifyUser instead")
      case None => authCollection += userObject(userName, password, userRole)
    }
  }

  // TODO: This needs to move into the db project
  def modifyUser(userName: String, password: String, userRole: String) = {
    val findUserQuery = findUser(userName)
    authCollection.findOne(findUserQuery) match {
      case None => logger.error("User %s does not exist. Please use addUser instead")
      case Some(dbo) => authCollection.findAndModify(findUserQuery, userObject(userName, password, userRole))
    }
  }

  // TODO: These functions need to move into the db project and be accessed from a (package?) object
  /**
   * Generate a user object
   */
  private[this] val userObject = (userName: String, plainPassword: String, userRole: String) => MongoDBObject("user" -> userName, "hashPw" -> hash(plainPassword), "userRole" -> userRole)

  /**
   * Generate a MongoDBObject that will find a user by name
   */
  private[this] val findUser = (userName: String) => MongoDBObject("user" -> userName)

  /**
   * Generate a MongoDBObject that will find a user by name nad hashed password
   */
  private[this] val authUserQuery = (userName: String, plainPassword: String) => MongoDBObject("user" -> userName, "hashPw" -> hash(plainPassword))

  /**
   * Get the MD5 hash of a string as a String
   */
  private[this] val hash = (input: String) => new String(MessageDigest.getInstance("MD5").digest(input.getBytes))

  /**
   * The collection to use for access and storage of authentication information
   */
  private[this] lazy val authCollection = MongoConfig.getCollection("AuthDb", "Authentication")
}