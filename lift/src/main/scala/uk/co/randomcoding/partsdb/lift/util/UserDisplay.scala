/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util

import scala.xml.NodeSeq

import uk.co.randomcoding.partsdb.core.user.User
import uk.co.randomcoding.partsdb.lift.util._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object UserDisplay extends TabularEntityDisplay {
  type EntityType = User

  override val rowHeadings = List("User Name", "User Role")

  /**
   * Display the `<td>` elements for a user
   */
  override def displayEntity(userDetails: User, editLink: Boolean, displayLink: Boolean): NodeSeq = {
    <td>{ userDetails.username.get }</td>
    <td>{ userDetails.role.get }</td> ++
      editAndDisplayCells("User", userDetails.id.get, editLink, displayLink)
  }
}