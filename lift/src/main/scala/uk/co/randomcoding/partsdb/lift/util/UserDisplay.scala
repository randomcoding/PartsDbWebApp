/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util
import scala.xml.NodeSeq

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object UserDisplay {
  val userHeadings = List("User Name", "User Role")

  def displayUser(userName: String, userRole: String): NodeSeq = {
    <tr><td>{ userName }</td><td>{ userRole }</td></tr>
  }
}