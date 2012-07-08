/*
 * Copyright (C) 2012 RandomCoder <randomcoder@randomcoding.co.uk>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *    RandomCoder - initial API and implementation and/or initial documentation
 */

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
