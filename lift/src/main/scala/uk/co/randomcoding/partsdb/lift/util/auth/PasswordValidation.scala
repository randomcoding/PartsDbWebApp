/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.auth

/**
 * Helper functions and such like for validating passwords
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object PasswordValidation {

  private val isUpperCase = (input: String) => if (input filter (_.isLower) isEmpty) Some("Password cannot be all upper case") else None

  private val isLowerCase = (input: String) => if (input filterNot (_.isLower) isEmpty) Some("Password cannot be all lower case") else None

  private val longEnough = (input: String, minLength: Int) => if (input.length <= minLength) Some("Password should be at least %d characters long".format(minLength)) else None

  private val matchesConfirmation = (input: String, confirm: String) => if (input != confirm) Some("Passwords and confirmation do not match") else None

  /**
   * List of validation partial functions used by `passwordErrors`
   */
  private val validations = (confirmation: String, minLength: Int) => List(matchesConfirmation(_: String, confirmation), isUpperCase, isLowerCase, longEnough(_: String, minLength))

  /**
   * Get the validation errors
   */
  val passwordErrors = (password: String, confirmation: String, minLength: Int) => validations(confirmation, minLength) map (_(password)) filterNot (_ == None) map (_.get)
}