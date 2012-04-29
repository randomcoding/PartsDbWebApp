/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet

import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.S
import net.liftweb.util.Helpers._

import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._

/**
 * This snippet provides basic functionality to render to buttons, '''Submit''' and '''Cancel'''
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait SubmitAndCancelSnippet {
  /**
   * The location this page was reached from
   */
  val cameFrom: String

  /**
   * Renders buttons in the divs (or spans) with ids ''submit'' and ''cancel''
   */
  def renderSubmitAndCancel() = {
    "#submit" #> styledButton("Submit", processSubmit) &
      "#cancel" #> styledButton("Cancel", processCancel)
  }

  /**
   * This function is called when the '''Submit''' button is pressed.
   *
   * Override this to do something with the data in the page
   */
  def processSubmit(): JsCmd

  /**
   * Function called when the '''Cancel''' button is pressed
   */
  def processCancel(): JsCmd = S redirectTo cameFrom
}