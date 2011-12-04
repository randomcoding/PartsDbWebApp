/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util

import scala.xml.{ Text, NodeSeq }

import net.liftweb.util.Helpers._
import net.liftweb.http._
import SHtml.{ span, link, ElemAttr }
import js.JsCmds.Noop

/**
 * Provides common helper functions for generating elements for transformations
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object TransformHelpers {

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
}