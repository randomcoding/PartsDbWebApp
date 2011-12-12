/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util
import scala.xml.NodeSeq
import scala.xml.Text

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object UserDisplay extends EntityDisplay {
  type EntityType = (String, String)

  override val rowHeadings = List("User Name", "User Role")

  /**
   * Display the `<td>` elements for a user
   */
  override def displayEntity(userDetails: (String, String)): NodeSeq = {
    <td>{ userDetails._1 }</td>
    <td>{ userDetails._2 }</td> ++
      editEntityCell(Text("Edit User"))
  }
}