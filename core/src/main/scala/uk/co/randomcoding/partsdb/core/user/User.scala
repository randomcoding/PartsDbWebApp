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
package uk.co.randomcoding.partsdb.core.user

import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.record.field.StringField
import com.foursquare.rogue.Rogue._
import net.liftweb.record.field.EnumField
import Role._
import Role.Role

/**
 * Simple User class
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class User private () extends MongoRecord[User] with ObjectIdPk[User] {
  def meta = User

  /**
   * Field to hold the username
   */
  object username extends StringField(this, 30)

  /**
   * Field to hold the hashed password
   */
  object password extends StringField(this, 128)

  /**
   * The role of this user
   */
  object role extends EnumField(this, Role)

  override def toString() = "User: [username: %s, role: %s]".format(username.get, role.get)

  /**
   * Two users are equals iff their `username` and `role` fields are the same.
   *
   * @param that The object to test for equality
   * @return `true` if the objects are equal `false` if not
   */
  override def equals(that: Any): Boolean = that match {
    case other: User => username.get == other.username.get && role.get == other.role.get
    case _ => false
  }

  override def hashCode = getClass.hashCode() + role.get.hashCode() + username.get.hashCode
}

object User extends User with MongoMetaRecord[User] {

  import org.bson.types.ObjectId

  /**
   * Create a user record but do not add it to the database
   *
   * @param userName The name for the new user
   * @param hashedPassword The hashed version of the password to store in the database
   * @param role The role of the user, either an Admin or normal User
   *
   * @return The newly created user
   */
  def apply(userName: String, hashedPassword: String, role: Role) = User.createRecord.username(userName).password(hashedPassword).role(role)

  /**
   * Find a user record by the record's Object Id
   *
   * @return A populated option if there is a User record with the given object id, or None if there is not one
   */
  def findById(oid: ObjectId) = User where (_.id eqs oid) get

  /**
   * Find a user by name
   *
   * @return A populated option if there is a User record with the given name, or None if there is not one
   */
  def findUser(userName: String): Option[User] = User where (_.username eqs userName) get

  /**
   * Modify a user record
   *
   * To keep the same value for a field, you need to provide the existing value from the user record.
   *
   * @param originalName The name of the user record to modify
   * @param newName The new name to assign to the user
   * @param newHashedPassword The new password to give the user
   * @param newRole The new role to give the user.
   */
  def modify(originalName: String, newName: String, newHashedPassword: String, newRole: Role): Option[User] = {
    User.where(_.username eqs originalName).modify(_.username setTo newName).and(_.password setTo newHashedPassword).and(_.role setTo newRole).updateMulti
    User.where(_.username eqs newName).get
  }

  /**
   * Find a user record that matches the provided one.
   *
   * A match is made if there is a record with the same Object Id or the same User Name as the provided record
   *
   * @param user The User record to locate a match for
   *
   * @return An optional User record if there is a match or None otherwise
   */
  def findMatching(user: User): Option[User] = findById(user.id.get) match {
    case Some(u) => Some(u)
    case _ => findUser(user.username.get)
  }

  /**
   * Add a user record unless a matching one already exists
   *
   * @return An optional User record if the add is successful or None if it failed or if there was a matching record
   */
  def addUser(user: User): Option[User] = findMatching(user) match {
    case None => user.role.get match {
      case ADMIN => user.saveTheRecord
      case USER => user.saveTheRecord
      case _ => None
    }
    case _ => None
  }

  @deprecated("1.0.1", "Use addUser(User) instead")
  def addUser(userName: String, hashedPassword: String, role: Role): Option[User] = findUser(userName) match {
    case None => role match {
      case NO_ROLE => None
      case _ => User(userName, hashedPassword, role).saveTheRecord
    }
    case _ => None
  }

  /**
   * Attempt to authenticate a user and password pair
   *
   * @param userName The username to attempt to authenticate as
   * @param hashedPassword The pre-hashed password to attempt the authentication with
   *
   * @return The user record (as an option) if there is a record with the given name and password
   */
  def authenticate(userName: String, hashedPassword: String): Option[User] = User where (_.username eqs userName) and (_.password eqs hashedPassword) get

  /**
   * Remove a user record if it exists
   *
   * @param user The user record to remove
   *
   * @return `true` if the removal was successful or `false` if there was no record to remove or if the remove failed
   */
  def remove(user: User): Boolean = findMatching(user) match {
    case Some(u) => u.delete_!
    case _ => false
  }

  /**
   * Remove a user record if it exists
   *
   * @param username The name of the user record to remove
   */
  @deprecated("1.0.1", "Use remove(User) instead")
  def remove(username: String): Boolean = findUser(username) match {
    case Some(user) => user.delete_!
    case _ => false
  }
}

