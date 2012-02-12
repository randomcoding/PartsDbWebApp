/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet

import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.contact.ContactDetails
import uk.co.randomcoding.partsdb.core.util.CountryCodes.matchToCountryCode
import uk.co.randomcoding.partsdb.lift.util.snippet._

import net.liftweb.common.Logger

/**
 * Validates form input items.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait DataValidation extends Logger {
  /**
   * Validates the input items and returns a list of error tuples
   *
   * Will return false for:
   *   - Empty Strings
   *   - Numeric values less than zero
   *   - values of `None`
   *
   * If the type is not handled, then will return true and output a debug message
   *
   * @param items The [[uk.co.randomcoding.partsdb.lift.util.snippet.ValidationItem]]s to validate
   * @return A list of `(String, String)` tuples if any item fails its validation. The tuples contain the `errorLocationId` and `errorMessage`
   * of the [[uk.co.randomcoding.partsdb.lift.util.snippet.ValidationItem]]s that failed.
   */
  def validate(items: ValidationItem*): List[(String, String)] = {
    items.toList filterNot (validateItem(_)) map (item => (item.errorLocationId, item.errorMessage))
  }

  private def validateItem(item: ValidationItem): Boolean = {
    trace("Validating: %s".format(item))
    item.toValidate match {
      // If we have a populated option value, recursively call this method with the item unwrapped
      case Some(thing) => validateItem(ValidationItem(thing, item.errorLocationId, item.errorMessage))
      case addr: Address => validateAddress(addr)
      case contact: ContactDetails => validateContactDetails(contact)
      case string: String => string.trim nonEmpty
      case double: Double => double >= 0.0
      case int: Int => int >= 0
      case None => {
        trace("Received an empty Option in %s. Assuming validation is false".format(item))
        false
      }
      case validationItem => {
        error("Unhandled validation type %s. Assuming it is valid".format(validationItem))
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
   * @return `true` if the address is not a [[uk.co.randomcoding.partsdb.core.address.NullAddress]] and has a valid entry for country
   */
  private def validateAddress(address: Address) = {
    val countryCodeIsOk = (address: Address) => matchToCountryCode(address.country.get).isDefined

    val shortNameIsOk = (address: Address) => addressShortNameChecks map (_(address.shortName.get)) contains (false) == false

    countryCodeIsOk(address) && shortNameIsOk(address)
  }

  /**
   * Functions used to validate address short names.
   *
   * Each function should return `false` if the short name does not validate
   */
  private[this] val addressShortNameChecks = List((shortName: String) => shortName.nonEmpty, (shortName: String) => shortName.replace("Business Address", "").trim.nonEmpty)

  /**
   * Validates [[uk.co.randomcoding.partsdb.core.contact.ContactDetails]]
   *
   * They are valid if they contain a name and at least one of `phoneNumbers`, `mobileNumbers` or `emailAddresses` contains a value.
   *
   * @param contacts The contact details to validate
   * @return `true` If the contact details validate
   */
  private def validateContactDetails(contacts: ContactDetails) = {

    def areValid(details: String*): Boolean = details filter (_.trim.nonEmpty) nonEmpty

    contacts.contactName.get.trim match {
      case "" => false
      case _ => areValid(contacts.phoneNumber.get, contacts.mobileNumber.get, contacts.emailAddress.get)
    }

  }
}

