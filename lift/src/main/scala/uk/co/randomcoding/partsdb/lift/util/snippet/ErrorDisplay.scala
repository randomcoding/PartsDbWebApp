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
