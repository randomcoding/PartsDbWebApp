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
  def displayError(formId: String, errorMessage: String): Unit = S.error(formId, errorMessage)

  /**
   * Delegate to [[uk.co.randomcoding.partsdb.lift.util.snippet.ErrorDisplay#displayError(String,String)]]
   *
   * @param errors One or more `Tuple2[String, String]` which are mapped to the parameters of the delegated method
   */
  def displayError(errors: (String, String)*): Unit = errors foreach { error => displayError(error._1, error._2) }

  /**
   * Remove all errors from the display
   */
  def clearErrors: Unit = S.error(Nil)

}