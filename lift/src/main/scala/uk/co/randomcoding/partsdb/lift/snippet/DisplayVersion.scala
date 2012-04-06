/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import net.liftweb.util.Helpers._
import scala.xml.Text
import net.liftweb.util.Props

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object DisplayVersion {
  def render = {
    "#versionNumber" #> Text(Props.get("app.version.number", "version not set"))
  }
}