package uk.co.randomcoding.partsdb.lift.snippet

/**
 * @author Jane Rowe
 */

import uk.co.randomcoding.partsdb.core.part.Part._
import uk.co.randomcoding.partsdb.core.part.Part
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
import uk.co.randomcoding.partsdb.db.mongo.MongoAllOrOneAccess
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle
import uk.co.randomcoding.partsdb.core.part.PartCost
import com.foursquare.rogue.Rogue._

class AddPart extends StatefulSnippet with ErrorDisplay with DataValidation with Logger {

  val cameFrom = S.referer openOr "/app/show?entityType=Part"
  var partName = ""
  var modId = ""

  var partCosts: Option[List[PartCost]] = None

  var vehicle: Option[Vehicle] = None
  val allVehicles: List[(Option[Vehicle], String)] = (None, "-- Select Vehicle --") :: {
    (Vehicle where (_.vehicleName exists true) orderDesc (_.vehicleName) fetch) map (v => (Some(v), v.vehicleName.get))
  }

  def dispatch = {
    case "render" => render
  }

  def render = {
    "#formTitle" #> Text("Add Part") &
      "#nameEntry" #> styledText(partName, partName = _) &
      "#vehicleEntry" #> styledObjectSelect[Option[Vehicle]](allVehicles, vehicle, vehicle = _) &
      "#modIdEntry" #> styledText(modId, modId = _) &
      "#submit" #> button("Submit", processSubmit)
  }

  /**
   * Method called when the submit button is pressed.
   *
   * This extracts the details required to make the Part object and if they validate, adds them to the database.
   *
   * On successful addition, this will (possibly display a dialogue and then) redirect to the main customers page
   */
  private[this] def processSubmit() = {
    val validationChecks = Seq(
      ValidationItem(partName, "partNameError", "Part Name must be entered"),
      ValidationItem(vehicle, "partVehicleError", "Vehicle is not valid"))

    validate(validationChecks: _*) match {
      case Nil => {
        add(partName, vehicle.get, modId.trim match {
          case "" => None
          case s: String => Some(s)
        })
        S redirectTo "/app/show?entityType=Part"
      }
      case errors => {
        errors foreach (error => displayError(error._1, error._2))
        Noop
      }
    }
  }

}