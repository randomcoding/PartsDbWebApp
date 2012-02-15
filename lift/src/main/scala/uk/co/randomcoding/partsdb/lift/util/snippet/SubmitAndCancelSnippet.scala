/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet

import net.liftweb.util.Helpers._
import net.liftweb.http.SHtml.button
import net.liftweb.http.js.JsCmd
import net.liftweb.http.S

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
    "#submit" #> button("Submit", processSubmit) &
      "#cancel" #> button("Cancel", processCancel)
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