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
   * @param formId The id of the element on the form web page to display the error at.
   * @param errorMessage The text of the error message to display
   */
  @deprecated("Use displayError(String) instead", "0.1")
  def displayError(formId: String, errorMessage: String): Unit = displayError(errorMessage)

  /**
   * Displays an error message from a snippet
   *
   * @param errorMessage The text of the error message to display
   */
  def displayError(errorMessage: String): Unit = S.error(ERROR_MESSAGE_ELEMENT_ID, errorMessage)

  /**
   * Display a series of error messages
   */
  def displayErrors(errorMessages: String*): Unit = errorMessages foreach displayError

  /**
   * Delegate to [[uk.co.randomcoding.partsdb.lift.util.snippet.ErrorDisplay#displayError(String,String)]]
   *
   * @param errors One or more `Tuple2[String, String]` which are mapped to the parameters of the delegated method
   */
  @deprecated("Use displayErrors(String*) instead", "0.1")
  def displayError(errors: (String, String)*): Unit = errors foreach { error => displayError(error._2) }

  /**
   * Remove all errors from the display
   */
  def clearErrors: Unit = S.error(Nil)

}