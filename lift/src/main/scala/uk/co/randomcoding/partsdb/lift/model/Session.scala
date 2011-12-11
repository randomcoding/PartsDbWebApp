/**
 *
 */
package uk.co.randomcoding.partsdb.lift.model

import net.liftweb.http.SessionVar

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object Session {
  object currentUser extends SessionVar[(String, String)](("", ""))
}