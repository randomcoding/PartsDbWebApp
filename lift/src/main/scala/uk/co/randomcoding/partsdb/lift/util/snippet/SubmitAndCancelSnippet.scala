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
package uk.co.randomcoding.partsdb.lift.util.snippet

import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.S
import net.liftweb.util.Helpers._

import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._

/**
 * This snippet provides basic functionality to render to buttons, '''Submit''' and '''Cancel'''
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait SubmitAndCancelSnippet {
  /**
   * The location this page was reached from
   */
  val cameFrom: () => String

  /**
   * Renders buttons in the divs (or spans) with ids ''submit'' and ''cancel''
   */
  def renderSubmitAndCancel() = {
    "#submit" #> styledButton("Submit", processSubmit) &
      "#cancel" #> styledButton("Cancel", processCancel)
  }

  /**
   * This function is called when the '''Submit''' button is pressed.
   *
   * Override this to do something with the data in the page
   */
  def processSubmit(): JsCmd

  /**
   * Function called when the '''Cancel''' button is pressed
   */
  def processCancel(): JsCmd = S redirectTo cameFrom()
}
