/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import scala.xml.NodeSeq

import net.liftweb.http.SHtml.ElemAttr.pairToBasic
import net.liftweb.http.{ SHtml, S }
import net.liftweb.util.Helpers.{ strToSuperArrowAssoc, strToCssBindPromoter }

import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._

/**
 * Simple snippet that displays Add or Edit buttons.
 *
 * This snippet expects a parametr called ''entityType'' that determines the
 * text that is displayed and the link that is generated.
 *
 * It is also expecting `<div>` elements with two specific ids:
 *  * `add`
 *  * `edit`
 * Which are transformed into the add and edit links respectively.
 *
 * == Example
 * {{{
 * <div class="lift:AddOrEdit?entityType=Customer">
 *   <div id="add">Add Link is created here</div>
 *   <div id="edit">Edit Link is created here</div>
 * </div>
 * }}}
 *
 * will create the html
 *
 * {{{
 * <div class="lift:AddOrEdit?entityType=Customer">
 *   <a href="add?entityType=Customer" class="button"><span>Add Customer</span></a></div>
 *   <a href="edit?entityType=Customer" class="button"><span>Edit Customer</span></a></div>
 * </div>
 * }}}
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddOrEdit {

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

    "#add" #> buttonLink("add?entityType=%s".format(entityType), addText) &
      "#edit" #> buttonLink("edit?entityType=%s".format(entityType), editText)
  }

}