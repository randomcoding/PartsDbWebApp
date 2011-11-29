/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet

import net.liftweb.http.S

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait ErrorDisplay {

  /**
   * Displays an error message from a snippet
   *
   * @param formId The id of the element on the form web page to display the error at.
   * @param errorMessage The text of the error message to display
   */
  def displayError(formId: String, errorMessage: String): Unit = {
    S.error(formId, errorMessage)
  }

}