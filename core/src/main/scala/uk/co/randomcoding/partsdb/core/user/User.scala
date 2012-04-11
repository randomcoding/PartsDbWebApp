/**
 *
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
class User private() extends MongoRecord[User] with ObjectIdPk[User] {
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

  def findById(oid: ObjectId) = User where (_.id eqs oid) get

  def findUser(userName: String): Option[User] = User where (_.username eqs userName) get

  def modify(originalName: String, newName: String, newHashedPassword: String, newRole: Role) {
    User where (_.username eqs originalName) modify (_.username setTo newName) and (_.password setTo newHashedPassword) and (_.role setTo newRole) updateMulti
  }

  def addUser(userName: String, hashedPassword: String, role: Role) = findUser(userName) match {
    case None => role match {
      case NO_ROLE => None
      case _ => Some(User.createRecord.username(userName).password(hashedPassword).role(role).save)
    }
    case _ => None
  }

  def authenticate(userName: String, hashedPassword: String) = User where (_.username eqs userName) and (_.password eqs hashedPassword) get

  def remove(username: String): Boolean = findUser(username) match {
    case Some(user) => user.delete_!
    case _ => false
  }
}

