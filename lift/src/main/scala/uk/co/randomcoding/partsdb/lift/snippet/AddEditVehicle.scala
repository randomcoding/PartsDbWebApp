package uk.co.randomcoding.partsdb.lift.snippet

/**
 * @author Jane Rowe
 */

import scala.xml.Text

import org.bson.types.ObjectId

import uk.co.randomcoding.partsdb.core.vehicle.Vehicle.add
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet._

import net.liftweb.common.{ Logger, Full }
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.http.{ StatefulSnippet, S }
import net.liftweb.util.Helpers._

class AddEditVehicle extends StatefulSnippet with ErrorDisplay with SubmitAndCancelSnippet with DataValidation with Logger {

  val initialVehicle = S param ("id") match {
    case Full(id) => Vehicle findById (new ObjectId(id))
    case _ => None
  }

  override val cameFrom = S.referer openOr "/app/show?entityType=Vehicle"

  var vehicleName = initialVehicle match {
    case Some(v) => v.vehicleName.get
    case _ => ""
  }

  def dispatch = {
    case "render" => render
  }

  def render = {
    "#formTitle" #> Text("Add Vehicle") &
      "#nameEntry" #> styledText(vehicleName, vehicleName = _) &
      renderSubmitAndCancel()
  }

  /**
   * Method called when the submit button is pressed.
   *
   * This extracts the details required to make the Vehicle object and if they validate, adds them to the database.
   *
   * On successful addition, this will (possibly display a dialogue and then) redirect to the main customers page
   */
  override def processSubmit() = {

    val validationChecks = Seq(
      ValidationItem(vehicleName, "errorMessages", "Vehicle Name must be entered"))

    validate(validationChecks: _*) match {
      case Nil => {
        initialVehicle match {
          case Some(v) => Vehicle.modify(v.vehicleName.get, vehicleName)
          case _ => add(vehicleName)
        }
        S redirectTo "/app/show?entityType=Vehicle"
      }
      case errors => {
        errors foreach (error => displayError(error._1, error._2))
        Noop
      }
    }
  }

}
