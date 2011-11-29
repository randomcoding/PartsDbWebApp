/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet

import uk.co.randomcoding.partsdb.core.terms.PaymentTerms
import uk.co.randomcoding.partsdb.core.address.NullAddress
import net.liftweb.common.Logger
import uk.co.randomcoding.partsdb.core.address.Address

/**
 * Validates form input items.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait DataValidation extends Logger {
  /**
   * Validates the input items and returns a list of error tuples
   *
   * @param items The [[uk.co.randomcoding.partsdb.lift.util.snippet.ValidationItem]]s to validate
   * @return A list of `(String, String)` tuples if any item fails its validation. The tuples contain the `errorLocationId` and `errorMessage`
   * of the [[uk.co.randomcoding.partsdb.lift.util.snippet.ValidationItem]]s that failed.
   */
  def validate(items: ValidationItem*): List[(String, String)] = {
    items.toList filterNot (validateItem(_)) map (item => (item.errorLocationId, item.errorMessage))
  }

  private def validateItem(item: ValidationItem): Boolean = {
    item.toValidate match {
      case addr: Address => NullAddress == addr
      case terms: PaymentTerms => terms == PaymentTerms(-1)
      case validationItem => {
        debug("Unhandled validation type %s".format(validationItem))
        true
      }
    }
  }
}

/**
 * An item that can be checked for a valid status.
 *
 * @constructor Create a new item to check for validation
 * @param toValidate The actual item to validate
 * @param errorLocationId The id of the element on the web page to display the error message at
 * @param errorMessage The message to display
 */
case class ValidationItem(toValidate: AnyRef, errorLocationId: String, errorMessage: String)