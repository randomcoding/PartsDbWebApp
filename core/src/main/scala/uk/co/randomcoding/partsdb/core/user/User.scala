/**
 *
 */
package uk.co.randomcoding.partsdb.core.user

import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.record.field.StringField
import net.liftweb.mongodb.record.field.Password
import net.liftweb.record.field.BooleanField
import net.liftweb.mongodb.record.field.MongoCaseClassField
import com.foursquare.rogue.Rogue._
import net.liftweb.record.field.EnumField
import Role._

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

  override def toString = "User: [username: %s, role: %s]".format(username.get, role.get)
}

object User extends User with MongoMetaRecord[User] {

  def findUser(userName: String): Option[User] = User where (_.username eqs userName) get

  def addUser(userName: String, hashedPassword: String, role: Role.Role) = role match {
    case NO_ROLE => None
    case _ => Some(User.createRecord.username(userName).password(hashedPassword).role(role).save)
  }

  def authenticate(userName: String, hashedPassword: String) = User where (_.username eqs userName) and (_.password eqs hashedPassword) get

  def remove(username: String): Boolean = findUser(username) match {
    case Some(user) => user.delete_!
    case _ => false
  }
}

