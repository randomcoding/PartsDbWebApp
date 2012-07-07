/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet

import net.liftweb.http.S

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait ErrorDisplay {
  private[this] val ERROR_MESSAGE_ELEMENT_ID = "errorMessages"

  /**
   * Displays an error message from a snippet
   *
   * @param errorMessage The text of the error message to display
   */
  def displayError(errorMessage: String): Unit = displayErrors(errorMessage)

  /**
   * Display a series of error messages
   */
  def displayErrors(errorMessages: String*): Unit = {
    val errorNodes = errorMessages map (message => <li class="error-message">{ message }</li>)

    S.error(ERROR_MESSAGE_ELEMENT_ID, <ul>{ errorNodes }</ul>)
  }

  /**
   * Remove all errors from the display
   */
  def clearErrors: Unit = S.error(ERROR_MESSAGE_ELEMENT_ID, Nil)
}