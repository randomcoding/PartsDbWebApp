package uk.co.randomcoding.partsdb.lift.snippet

/**
 * @author Jane Rowe
 */

import uk.co.randomcoding.partsdb.core.vehicle.Vehicle._
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet.{ ValidationItem, ErrorDisplay, DataValidation, StyleAttributes }
import uk.co.randomcoding.partsdb.lift.util.snippet.StyleAttributes._
import net.liftweb.common.StringOrNodeSeq.strTo
import net.liftweb.common.{ Logger, Full }
import net.liftweb.http.SHtml.{ select, button }
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.http.js.JsCmd
import net.liftweb.http.S
import net.liftweb.util.Helpers._
import scala.xml.Text
import net.liftweb.http.StatefulSnippet
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle
import uk.co.randomcoding.partsdb.db.mongo.MongoAllOrOneAccess
import org.bson.types.ObjectId

class AddEditVehicle extends StatefulSnippet with ErrorDisplay with DataValidation with Logger {

  val initialVehicle = S param ("id") match {
    case Full(id) => Vehicle findById (new ObjectId(id))
    case _ => None
  }

  val cameFrom = S.referer openOr "/app/show?entityType=Vehicle"
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
      "#submit" #> button("Submit", processSubmit)
  }

  /**
   * Method called when the submit button is pressed.
   *
   * This extracts the details required to make the Vehicle object and if they validate, adds them to the database.
   *
   * On successful addition, this will (possibly display a dialogue and then) redirect to the main customers page
   */
  private[this] def processSubmit() = {

    val validationChecks = Seq(
      ValidationItem(vehicleName, "vehicleNameError", "Vehicle Name must be entered"))

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