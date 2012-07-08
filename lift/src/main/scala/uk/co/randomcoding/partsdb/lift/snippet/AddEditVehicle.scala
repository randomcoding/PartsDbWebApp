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
package uk.co.randomcoding.partsdb.lift.snippet

import scala.xml.Text

import uk.co.randomcoding.partsdb.core.util.MongoHelpers._
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle.{ add, modify, create }
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet._

import net.liftweb.common.{ Logger, Full }
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.http.{ StatefulSnippet, S }
import net.liftweb.util.Helpers._

/**
 * @author Jane Rowe
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddEditVehicle extends StatefulSnippet with ErrorDisplay with SubmitAndCancelSnippet with DataValidation with Logger {

  val initialVehicle = S param ("id") match {
    case Full(id) => Vehicle findById id
    case _ => None
  }

  override val cameFrom = () => "/app/show?entityType=Vehicle"

  var (vehicleName, pdfFile) = initialVehicle match {
    case Some(v) => (v.vehicleName.get, v.pdfFile.get)
    case _ => ("", "")
  }

  def dispatch = {
    case "render" => render
  }

  def render = {
    "#formTitle" #> Text("%s Vehicle".format(if (initialVehicle.isDefined) "Edit" else "Add")) &
      "#nameEntry" #> styledText(vehicleName, vehicleName = _) &
      "#pdfFileEntry" #> styledText(pdfFile, pdfFile = _) &
      renderSubmitAndCancel()
  }

  override def validationItems() = Seq(ValidationItem(vehicleName, "Vehicle Name"))

  /**
   * Method called when the submit button is pressed.
   *
   * This extracts the details required to make the Vehicle object and if they validate, adds them to the database.
   *
   * On successful addition, this will (possibly display a dialogue and then) redirect to the main customers page
   */
  override def processSubmit() = performValidation() match {
    case Nil => {
      initialVehicle match {
        case Some(v) => Vehicle.modify(v.id.get, Vehicle(vehicleName, pdfFile))
        case _ => add(create(vehicleName, pdfFile))
      }
      S redirectTo "/app/show?entityType=Vehicle"
    }
    case errors => {
      displayErrors(errors: _*)
      Noop
    }
  }

}
