/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet.print

import net.liftweb.http.StatefulSnippet
import net.liftweb.util.Helpers._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class PrintDocument extends StatefulSnippet {

  def dispatch = {
    case "render" => render
  }

  def render = {
    "#id" #> "Stuff"
  }
}