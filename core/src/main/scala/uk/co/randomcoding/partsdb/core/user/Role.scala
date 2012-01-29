/**
 *
 */
package uk.co.randomcoding.partsdb.core.user

/**
 * Case object hierarchy for User Roles
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object Role extends Enumeration("user", "admin") {
  type Role = Value
  val USER, ADMIN = Value

  implicit def stringToRole(roleString: String): Role = roleString.trim toLowerCase match {
    case "admin" => ADMIN
    case _ => USER
  }
}

