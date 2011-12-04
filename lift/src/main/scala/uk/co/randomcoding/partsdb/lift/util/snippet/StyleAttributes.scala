/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet

import net.liftweb.http.SHtml.ElemAttr

/**
 * Contains definitions of commonly used `ElemAttr`s.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object StyleAttributes {

  /**
   * Applies the required class styles to a widget to be styled by JQueryUI's css
   */
  val jqueryUiTextStyled: ElemAttr = "class" -> "ui-widget ui-state-default ui-corner-all"
}