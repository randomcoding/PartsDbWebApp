/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util

import scala.xml.NodeSeq
import scala.xml.Text
import uk.co.randomcoding.partsdb.core.user.User

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object UserDisplay extends EntityDisplay {
  type EntityType = User

  override val rowHeadings = List("User Name", "User Role")

  /**
   * Display the `<td>` elements for a user
   */
  override def displayEntity(userDetails: User): NodeSeq = {
    <td>{ userDetails.username.get }</td>
    <td>{ userDetails.role.get }</td> ++
      editEntityCell(editEntityLink("User", userDetails.id.get))
  }
}