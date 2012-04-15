/**
 *
 */
package uk.co.randomcoding.partsdb.lift.model

import net.liftweb.http.SessionVar
import uk.co.randomcoding.partsdb.core.user.Role.{ NO_ROLE, Role }
import uk.co.randomcoding.partsdb.core.transaction.Payment

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object Session {
  object currentUser extends SessionVar[(String, Role)](("", NO_ROLE))

  object recentNewPayments extends SessionVar[Seq[Payment]](Nil)
}