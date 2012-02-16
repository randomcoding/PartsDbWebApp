/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util

import scala.xml.{ Text, NodeSeq }
import uk.co.randomcoding.partsdb.lift.util.snippet.StyleAttributes._
import net.liftweb.common.Full
import net.liftweb.http.SHtml.ElemAttr.pairToBasic
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.util.Helpers._
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle
import net.liftweb.http.js.JsCmd
import net.liftweb.util.ValueCell
import net.liftweb.http.WiringUI

/**
 * Provides common helper functions for generating elements for transformations
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object TransformHelpers {

  /**
   * Convenience definition of a typed Ajax callback function. This is used in the Ajax styled widget functions
   */
  type ajaxWidgetCallback[T] = (T) => JsCmd

  type emptyAjaxWidgetCallback = () => JsCmd

  /**
   * Convenience definition of a typed standard callback function. This is used in the standard styled widget functions
   */
  type standardWidgetCallback[T] = (T) => Any

  /**
   * Creates a link that has the additional class of button and an inner `<span>` element. This will cause it to be rendered as a button by the default css.
   *
   * @param linkText The text that it to be inside the button
   * @param linkTarget The url of the destination of the link
   * @return a [[scala.xml.NodeSeq]] for a link with an inner span and the class attribute of the `<a>` element set to `button`
   */
  def buttonLink(linkText: String, linkTarget: String): NodeSeq = {
    attrLink(linkTarget, linkText, "class" -> "btn")
  }

  /**
   * Creates a plain link with an inner span element.
   *
   * @param linkText The text that it to be inside the button
   * @param linkTarget The url of the destination of the link
   */
  def plainLink(linkText: String, linkTarget: String): NodeSeq = {
    attrLink(linkTarget, linkText)
  }

  /**
   * Creates a link with an inner span element and the given attributes.
   *
   * @param linkText The text that it to be inside the button
   * @param linkTarget The url of the destination of the link
   * @param linkAttrs A varargs list of `"attributeName" -> "attributeValue"` pairs to add to the `<a>` element
   * @return a [[scala.xml.NodeSeq]] for a link with an inner span and the given element attributes set on the `<a>` element
   */
  def attrLink(linkText: String, linkTarget: String, linkAttrs: ElemAttr*): NodeSeq = {
    link(linkTarget, () => Unit, span(Text(linkText), Noop), linkAttrs: _*)
  }

  /**
   * Creates a Text Area with JQueryUI Styling
   *
   * @param initialText The initial value to display in the text area
   * @param func The function to call on form Submit. Commonly sets the value of a variable in the snippet
   * @param attrs Any additional attributes to apply to this text area
   */
  def styledTextArea(initialText: String, func: standardWidgetCallback[String], attrs: List[ElemAttr] = Nil): NodeSeq = {
    textarea(initialText, func, styledAttributes(attrs): _*)
  }

  /**
   * Creates a Text Box with JQueryUI Styling
   *
   * @param initialText The initial value to display in the text box
   * @param func The function to call on form Submit. Commonly sets the value of a variable in the snippet
   * @param attrs Any additional attributes to apply to this text box
   */
  def styledText(initialText: String, func: standardWidgetCallback[String], attrs: List[ElemAttr] = Nil): NodeSeq = {
    text(initialText, func, styledAttributes(attrs): _*)
  }

  /**
   * Creates a Text Box with JQueryUI Styling and an Ajax callback
   *
   * @param initialText The initial value to display in the text box
   * @param func The function to call on update (usually `onBlur`). Commonly sets the value of a variable in the snippet
   * @param attrs Any additional attributes to apply to this text box
   */
  def styledAjaxText(initialText: String, func: ajaxWidgetCallback[String], linkAttrs: List[ElemAttr] = Nil): NodeSeq = {
    ajaxText(initialText, func, styledAttributes(linkAttrs): _*)
  }

  /**
   * Creates a Password Entry with JQueryUI Styling
   *
   * @param initialText The initial value to display in the text box
   * @param func The function to call on form submit. Commonly sets the value of a variable in the snippet
   * @param attrs Any additional attributes to apply to this password entry
   */
  def styledPassword(initialText: String, func: standardWidgetCallback[String], linkAttrs: List[ElemAttr] = Nil): NodeSeq = {
    password(initialText, func, styledAttributes(linkAttrs): _*)
  }

  /**
   * Creates a Combo Box with JQueryUI Styling
   *
   * This maps a display string to a value string.
   *
   * @param values The ordered sequence of (display -> value) tuples to populate the combo box with
   * @param initialText The initial value to display in the combo box. This should be on of the display values from `values`
   * @param func The function to call on form submit. Commonly sets the value of a variable in the snippet
   * @param attrs Any additional attributes to apply to this combo box
   */
  def styledSelect(values: Seq[(String, String)], initialValue: String, func: standardWidgetCallback[String], linkAttrs: List[ElemAttr] = Nil): NodeSeq = {
    select(values, Full(initialValue), func, styledAttributes(linkAttrs): _*)
  }

  /**
   * Creates a Combo Box with JQueryUI Styling and an Ajax callback.
   *
   * This maps a display string to a value string.
   *
   * @param values The ordered sequence of (display -> value) tuples to populate the combo box with
   * @param initialText The initial value to display in the combo box. This should be on of the display values from `values`
   * @param func The function to call on update (usually `onBlur`. Commonly sets the value of a variable in the snippet.
   * The input parameter to this function is the ''value'' part of the selected entry from `values`
   * @param attrs Any additional attributes to apply to this combo box
   */
  def styledAjaxSelect(values: Seq[(String, String)], initialValue: String, func: ajaxWidgetCallback[String], linkAttrs: List[ElemAttr] = Nil): NodeSeq = {
    ajaxSelect(values, Full(initialValue), func, styledAttributes(linkAttrs): _*)
  }

  /**
   * Creates a Combo Box with JQueryUI Styling and an Ajax callback
   *
   * Maps display strings to object values.
   *
   * @param values The ordered sequence of (display -> value) tuples to populate the combo box with
   * @param initialText The initial value to display in the combo box. This should be on of the display values from `values`
   * @param func The function to call on update (usually `onBlur`. Commonly sets the value of a variable in the snippet.
   * The input parameter to this function is the ''value'' part of the selected entry from `values`
   * @param attrs Any additional attributes to apply to this combo box
   */
  def styledAjaxObjectSelect[T](values: Seq[(T, String)], initialValue: T, func: ajaxWidgetCallback[T], linkAttrs: List[ElemAttr] = Nil)(implicit mf: Manifest[T]): NodeSeq = {
    ajaxSelectObj(values, Full(initialValue), func, styledAttributes(linkAttrs): _*)
  }

  /**
   * Creates a Combo Box with JQueryUI Styling and an Ajax callback
   *
   * Maps display strings to object values.
   *
   * @param values The ordered sequence of (display -> value) tuples to populate the combo box with
   * @param initialText The initial value to display in the combo box. This should be on of the display values from `values`
   * @param func The function to call on update (usually `onBlur`. Commonly sets the value of a variable in the snippet.
   * The input parameter to this function is the ''value'' part of the selected entry from `values`
   * @param attrs Any additional attributes to apply to this combo box
   */
  def styledObjectSelect[T](values: Seq[(T, String)], initialValue: T, func: standardWidgetCallback[T], linkAttrs: List[ElemAttr] = Nil)(implicit mf: Manifest[T]): NodeSeq = {
    selectObj(values, Full(initialValue), func, styledAttributes(linkAttrs): _*)
  }

  /**
   * Creates a button with an Ajax callback
   *
   * @param buttonText The text to display on the button
   * @param func The function to call when the button is pressed
   * @param attrs Any additional attributes to apply to this button
   */
  def styledAjaxButton(buttonText: String, func: emptyAjaxWidgetCallback, linkAttrs: List[ElemAttr] = Nil): NodeSeq = {
    ajaxButton(Text(buttonText), func, styledAttributes(linkAttrs): _*)
  }

  def styledMultiSelectObj[T](values: Seq[(T, String)], initialValue: Seq[T], func: List[T] => Any, linkAttrs: List[ElemAttr] = Nil): NodeSeq = {
    multiSelectObj(values, initialValue, func, styledAttributes(linkAttrs): _*)
  }

  /**
   * Function that generates an ajax wrapper around setting a value for a variable.
   *
   * By default (without the JsCmd parameter) will perform a `Noop` after updating the value
   */
  def updateAjaxValue[T](updateFunc: (T) => Any, jscmd: JsCmd = Noop): T => JsCmd = {
    (t: T) =>
      {
        updateFunc(t)
        jscmd
      }
  }

  private def styledAttributes(attrs: List[ElemAttr]) = jqueryUiTextStyled :: attrs
}