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
package uk.co.randomcoding.partsdb.db.mongo

import uk.co.randomcoding.partsdb.core.user.{ User, Role }
import uk.co.randomcoding.partsdb.core.user.User._
import uk.co.randomcoding.partsdb.db.util.Helpers._
import net.liftweb.common.Loggable
import uk.co.randomcoding.partsdb.core.user.Role._

/**
 * Provides access to the user database for the app
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
@deprecated("1.0.1", "Use User access methodds directly")
object MongoUserAccess extends Loggable {
  /**
   * Add a new user to the user database.
   *
   * If there is already a user present with the same name then this will not add the user and return an error
   *
   * @param userName The name of the user to add. This is case sensitive
   * @param plainPassword The '''PLAINTEXT''' password for the user. This will be hashed when added to the database so don't forget it.
   * @param userRole The role the user will have within the Application. Currently expects ''user'' or ''admin''
   *
   * @return An optional error message. If this is defined then the user was not added to the database and the option contains the error message.
   * An undefined return value (i.e. `None` indicates success)
   */
  def addNewUser(userName: String, plainPassword: String, userRole: String): Option[String] = {
    val found = findUser(userName)
    found match {
      case None => addUser(userName, hash(plainPassword), userRole) match {
        case Some(user) => {
          logger.info("Added User %s".format(user))
          None // all ok
        }
        case _ => Some("Failed to create User '%s' with Role '%s'".format(userName, userRole))
      }
      case Some(user) => Some("Cannot add user '%s' as they already exist with role %s.".format(user.username.get, user.role.get))
    }
  }

  /**
   * Modify an existing user in the user database.
   *
   * If there is no user with the given name present then this '''''will not''''' add the user and return an error
   *
   * == WARNING ==
   * It is assumed that you have already verified the modify user operation before calling this method.
   *
   * @param userName The name of the user to modify. This is case sensitive and the user must already be present in the database
   * @param plainPassword The '''PLAINTEXT''' password for the user. This will be hashed when added to the database so don't forget it.
   * If this different to the current password then the user's password will be changed.
   * @param userRole The role the user will have within the Application. Currently expects ''user'' or ''admin''.
   * If this is different to the current user's role then it will be changed.
   *
   * @return An optional error message. If this is defined then the user was not modified and the option contains the error message.
   * An undefined return value (i.e. `None` indicates success)
   */
  def modifyUser(userName: String, plainPassword: String, userRole: String): Option[String] = {
    findUser(userName) match {
      case Some(user) => user.password(hash(plainPassword)).role(userRole).update match {
        case u: User if u.password.get == hash(plainPassword) && u.role.get.toString == userRole => None
        case _ => Some("Failed to update user %s".format(userName))
      }
      case _ => Some("User '%s' does not exist.".format(userName))
    }
  }

  /**
   * Attempt to authenticate a user. If successful, the user's [[uk.co.randomcoding.partsdb.core.user.Role]] is returned in an `Option`
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
  def authenticateUser(userName: String, hashedPassword: String): Option[Role] = {
    authenticate(userName, hashedPassword) match {
      case Some(user) => Some(user.role.get)
      case _ => None
    }
  }

  /**
   * Removed a user from the database.
   *
   *  The user is matched on user name are user role
   *
   *  @param userName The user name of the entry to remove
   *  @param userRole The role of the entry to remove
   *
   *  @return An optional string containing any error message. If this is empty (i.e. `None`) then the remove operation succeeded
   */
  def removeUser(userName: String, userRole: String): Option[String] = if (remove(userName)) None else Some("Failed to remove user '%s' with role '%s'".format(userName, userRole))

  /**
   * Get the users currently defined in the auth database.
   *
   * @return A list of pairs of usernames and user roles
   */
  def users: Map[String, Role] = User.findAll map (user => (user.username.get, user.role.get)) toMap
}
