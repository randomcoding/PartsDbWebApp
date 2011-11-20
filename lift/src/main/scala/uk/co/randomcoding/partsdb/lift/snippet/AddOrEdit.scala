/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import net.liftweb._
import http.{ StatefulSnippet, S, SHtml }
import util.Helpers._

/**
 * Simple snippet that displays Add or Edit buttons.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddOrEdit {
  def render = {
    val entityType = S.attr("entityType") openOr ("Unspecified")
    val addText = "Add " + entityType
    val editText = "View or Edit %s".format(entityType)

    "#add" #> SHtml.link("add" + entityType, () => Unit, <span>{ addText }</span>, "class" -> "button") &
      "#edit" #> SHtml.link("edit" + entityType, () => Unit, <span>{ editText }</span>, "class" -> "button")
  }
}