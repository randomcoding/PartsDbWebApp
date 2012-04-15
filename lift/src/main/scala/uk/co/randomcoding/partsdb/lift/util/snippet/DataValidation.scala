/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet

import scala.collection.Traversable

import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.contact.ContactDetails
import uk.co.randomcoding.partsdb.core.util.CountryCodes.matchToCountryCode

import net.liftweb.common.Logger

/**
 * Validates form input items.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait DataValidation extends Logger {

  /**
   * Method to perform actual validation.
   *
   * This will validate the [[uk.co.randomcoding.partsdb.lift.util.snippet.DataValidation#validationItems]] and then execute
   * each of the validation functions provided.
   *
   *  @param validationFuncs Functions `() => Seq[String]` that perform additional validation not covered by the
   *  [[uk.co.randomcoding.partsdb.lift.util.snippet.DataValidation#validationItems]]. These functions should return error message(s)
   *  contained in a `Seq[String]` if they fail or `Nil` if they pass validation.
   *  @return A `Seq` or error messages, or `Nil` if all items validated OK
   */
  def performValidation(validationFuncs: (() => Seq[String])*) = validate(validationItems(): _*) ++ (validationFuncs flatMap (_()))

  /**
   * Sequence of [[uk.co.randomcoding.partsdb.lift.util.snippet.ValidationItem]]s to validate on the given page
   */
  def validationItems(): Seq[ValidationItem]

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
  def validate(items: ValidationItem*): Seq[String] = items map (validateItem(_)) filter (_.isDefined) map (_.get) flatten

  /**
   * Perform the validation process.
   *
   * @return `None` if the item validates OK. Otherwise returns a populated `Option[String]`
   */
  private def validateItem(item: ValidationItem): Option[Seq[String]] = {
    debug("Validating: %s".format(item))
    item.toValidate match {
      // If we have a populated option value, recursively call this method with the item unwrapped
      case Some(thing) => validateItem(ValidationItem(thing, item.fieldName))
      case collection: Traversable[_] => if (collection.size == 0) Some(Seq("%s requires a non empty set of items".format(item.fieldName))) else None
      case addr: Address => validateAddress(addr)
      case contact: ContactDetails => validateContactDetails(contact)
      case string: String => validateString(string, item.fieldName)
      case double: Double => if (double >= 0.0) None else Some(Seq("%s requires a value of 0 or greater".format(item.fieldName)))
      case int: Int => if (int >= 0.0) None else Some(Seq("%s requires a value of 0 or greater".format(item.fieldName)))
      case None => {
        debug("Received an empty Option in %s. Assuming validation is false".format(item))
        Some(Seq("Received no value for %s".format(item.fieldName)))
      }
      case validationItem => {
        debug("Unhandled validation type %s. Assuming it is valid".format(validationItem))
        None
      }
    }
  }

  /**
   * Validate a string value is not empty
   */
  private[this] def validateString(string: String, fieldName: String) = string.trim nonEmpty match {
    case false => Some(Seq("%s requires a non empty value".format(fieldName)))
    case true => None
  }

  /**
   * Performs actual address validation.
   *
   * Checks for:
   *  * The country being a match in [[uk.co.randomcoding.partsdb.core.util.CountryCodes]]
   *  * The short name being ok see [[uk.co.randomcoding.partsdb.lift.util.snippet.DataValidation#addressShortNameChecks]]
   *
   * @param address The [[uk.co.randomcoding.partsdb.core.address.Address]] to validate
   * @return `true` if the address has a valid entry for country and the short name validates ok
   */
  private def validateAddress(address: Address) = {
    val countryCodeIsOk = (address: Address) => matchToCountryCode(address.country.get).isDefined

    val shortNameIsOk = (address: Address) => addressShortNameChecks map (_(address.shortName.get)) contains (false) == false

    val shortNameErrorMessage = "Address Short Name is not valid"
    val countryCodeErrorMessage = "Country Code %s is not valid".format(address.country.get)
    (countryCodeIsOk(address), shortNameIsOk(address)) match {
      case (true, true) => None
      case (true, false) => Some(Seq(shortNameErrorMessage))
      case (false, true) => Some(Seq(countryCodeErrorMessage))
      case (false, false) => Some(Seq(countryCodeErrorMessage, shortNameErrorMessage))
    }
  }

  /**
   * Functions used to validate address short names.
   *
   * Checks for the following:
   *  - The short name is not empty, or made up of whitespace only
   *  - The short name is not just **Business Address** plus whitespace
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
  private def validateContactDetails(contacts: ContactDetails): Option[Seq[String]] = {

    def areValid(details: Seq[String]): Boolean = details filter (_.trim.nonEmpty) nonEmpty

    contacts.contactName.get.trim match {
      case "" => Some(Seq("Contact requires a name"))
      case _ => areValid(Seq(contacts.phoneNumber.get, contacts.mobileNumber.get, contacts.emailAddress.get, contacts.faxNumber.get)) match {
        case true => None
        case false => Some(Seq("Contact Details requires at least one contact method to be entered"))
      }
    }
  }
}
