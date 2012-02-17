/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._

import net.liftweb.http.S
import net.liftweb.util.Helpers._

/**
 * Simple snippet that displays Add or Edit buttons.
 *
 * This snippet expects a parameter called ''entityType'' that determines the
 * text that is displayed and the link that is generated.
 *
 * It is also expecting `div` elements with two specific ids:
 *  - `add`
 *  - `edit`
 *
 * Which are transformed into the add and edit links respectively.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object AddOrEdit {

  /**
   * Transforms the `#add` and `#edit` divs into links with `add/edit?entityType=...` targets.
   *
   * @param entityType The type of entity that is to be added or edited. This value is used ''as is''
   * so should not contain spaces and any capitalisation is preserved.
   */
  def render = {
    val entityType = S.attr("entityType") openOr ("Unspecified")
    val addText = "Add " + entityType
    val editText = "View or Edit %s".format(entityType)

    "#add" #> buttonLink("add%s".format(entityType), addText) &
      "#edit" #> buttonLink("edit%s".format(entityType), editText)
  }

}