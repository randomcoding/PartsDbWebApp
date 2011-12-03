/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet

import uk.co.randomcoding.partsdb.core.terms.PaymentTerms
import uk.co.randomcoding.partsdb.core.address.NullAddress
import net.liftweb.common.Logger
import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.util.CountryCodes._

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
    debug("Validating: %s".format(item))
    item.toValidate match {
      case addr: Address => validateAddress(addr)
      case terms: PaymentTerms => terms != PaymentTerms(-1)
      case validationItem => {
        debug("Unhandled validation type %s. Assuming it is valid".format(validationItem))
        true
      }
    }
  }

  /**
   * Performs actual address validation.
   *
   * Checks for:
   *  * [[uk.co.randomcoding.partsdb.core.address.NullAddress]] => false
   *  * The country being a match in [[uk.co.randomcoding.partsdb.core.util.CountryCodes]]
   *
   * @param address The [[uk.co.randomcoding.partsdb.core.address.Address]] to validate
   * @return true if the address is not a [[uk.co.randomcoding.partsdb.core.address.NullAddress]] and has a valid entry for country
   */
  private def validateAddress(address: Address): Boolean = {
    address match {
      case NullAddress => false
      case Address(_, _, _, country) => matchToCountryCode(country).isDefined
    }
  }
}

