/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import com.mongodb.casbah.Imports._

import uk.co.randomcoding.partsdb.db.util.Helpers._

import net.liftweb.common.Loggable

/**
 * Provides access to the user database for the app
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object MongoUserAccess extends Loggable {
  /**
   * Add a new user to the user database.
   *
   * If there is already a user present with the same name then this will not add the user and return an error
   *
   * @param userName The name of the user to add. This is case sensitive
   * @param password The '''PLAINTEXT''' password for the user. This will be hashed when added to the database so don't forget it.
   * @param userRole The role the user will have within the Application. Currently expects ''user'' or ''admin''
   *
   * @return An optional error message. If this is defined then the user was not added to the database and the option contains the error message.
   * An undefined return value (i.e. `None` indicates success)
   */
  def addUser(userName: String, password: String, userRole: String): Option[String] = {
    authCollection.findOne(userByNameQuery(userName)) match {
      case Some(dbo) => {
        val errorMessage = "User %s already exists. Please use modifyUser instead"
        logger.error(errorMessage)
        Some(errorMessage)
      }
      case None => {
        authCollection += userObject(userName, password, userRole)
        None
      }
    }
  }

  /**
   * Modify an existing user in the user database.
   *
   * If there is no user with the given name present then this '''''will not''''' add the user and return an error
   *
   * == WARNING
   * It is assumed that you have already verified the modify user operation before calling this method.
   *
   * @param userName The name of the user to modify. This is case sensitive and the user must already be present in the database
   * @param password The '''PLAINTEXT''' password for the user. This will be hashed when added to the database so don't forget it.
   * If this different to the current password then the user's password will be changed.
   * @param userRole The role the user will have within the Application. Currently expects ''user'' or ''admin''.
   * If this is different to the current user's role then it will be changed.
   *
   * @return An optional error message. If this is defined then the user was not modified and the option contains the error message.
   * An undefined return value (i.e. `None` indicates success)
   */
  def modifyUser(userName: String, password: String, userRole: String): Option[String] = {
    val findUserQuery = userByNameQuery(userName)
    authCollection.findOne(findUserQuery) match {
      case None => {
        val errorMessage = "User %s does not exist. Please use addUser instead"
        logger.error(errorMessage)
        Some(errorMessage)
      }
      case Some(dbo) => {
        val response = authCollection.findAndModify(findUserQuery, userObject(userName, password, userRole)) match {
          case Some(dbo) if (dbo.getAs[String]("user")) == Some(userName) => None
          case Some(dbo) if (dbo.getAs[String]("user")) isDefined => Some("Updating user %s returned a user with name %s".format(userName, dbo.getAs[String]("user").get))
          case Some(dbo) => Some("Updating user %s returned unknown db object %s".format(userName, dbo))
          case _ => Some("Updating user %s returned empty db object".format(userName))
        }

        if (response.isDefined) logger.error(response.get)

        response
      }
    }
  }

  /**
   * Accessor for the ''role'' of an authenticated user
   *
   * This will return the ''userRole'' portion of the user data '''iff''' the user name ''and'' hashed password are located in the database together
   *
   * This therefore provides the authentication mechanism for the application as a valid login will contain a user name and hashed password that are found in the database.
   * The actual handling of the allow/reject is not handled by this however. (It is handled by the Lift Authentication mechanism in the '''''lift''''' project)
   *
   * @param userName The name of the user to find the role of
   * @param hashedPassword The hashed password that is stored in the database for the user
   *
   * @return An optional string. If this is defined then the user and hashed password have been found in the database and the user is authenticated into a specific role.
   * If this is undefined (i.e. `None`) then the user is not authenticated and should be rejected.
   */
  def userRole(userName: String, hashedPassword: String): Option[String] = {
    findAuthenticatedUser(userName, hashedPassword) match {
      case Some(dbo: DBObject) if (dbo.getAs[String]("userRole") isDefined) => dbo.getAs[String]("userRole")
      case _ => None
    }
  }

  /**
   * Locate a user in the database by name and hashed password
   */
  private def findAuthenticatedUser(userName: String, hashedPassword: String): Option[DBObject] = authCollection.findOne(authUserQuery(userName, hashedPassword))

  /**
   * Generate a MongoDBObject that will find a user by name
   */
  private[this] val userByNameQuery = (userName: String) => MongoDBObject("user" -> userName)

  /**
   * Generate a MongoDBObject that will find a user by name and hashed password, i.e. find the user is they are authenticated
   */
  private[this] val authUserQuery = (userName: String, plainPassword: String) => MongoDBObject("user" -> userName, "hashPw" -> hash(plainPassword))

  /**
   * Generate a user object that can be added to the database (or matched against)
   */
  private[this] val userObject = (userName: String, plainPassword: String, userRole: String) => MongoDBObject("user" -> userName, "hashPw" -> hash(plainPassword), "userRole" -> userRole)

  /**
   * The collection to use for access and storage of authentication information
   */
  private[this] lazy val authCollection = MongoConfig.getCollection("AuthDb", "Authentication")
}