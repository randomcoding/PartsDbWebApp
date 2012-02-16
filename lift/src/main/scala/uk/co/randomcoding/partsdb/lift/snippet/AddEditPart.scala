package uk.co.randomcoding.partsdb.lift.snippet

import scala.xml.Text
import org.bson.types.ObjectId
import com.foursquare.rogue.Rogue._
import uk.co.randomcoding.partsdb.core.part.Part.add
import uk.co.randomcoding.partsdb.core.part.Part
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet._
import net.liftweb.common.{ Logger, Full, Empty, Box }
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.http.{ StatefulSnippet, S }
import net.liftweb.util.Helpers._

/**
 * Snippet to add, or edit, a part
 *
 * @author Jane Rowe
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddEditPart extends StatefulSnippet with ErrorDisplay with DataValidation with SubmitAndCancelSnippet with Logger {

  /**
   * Check if we have been called with an id parameter or not and setup the initial part appropriately
   */
  val initialPart = S param ("id") match {
    case Full(id) => Part findById (new ObjectId(id))
    case _ => None
  }

  override val cameFrom = S.referer openOr "/app/show?entityType=Part"

  var (partName, vehicle, modId) = initialPart match {
    case Some(part) => (part.partName.get, Vehicle.findById(part.vehicle.get), part.modId.get.getOrElse(""))
    case _ => ("", None, "")
  }

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
      renderSubmitAndCancel()
  }

  /**
   * Method called when the submit button is pressed.
   *
   * This extracts the details required to make the Part object and if they validate, adds them to the database.
   *
   * On successful addition, this will (possibly display a dialogue and then) redirect to the main customers page
   */
  override def processSubmit() = {
    val validationChecks = Seq(
      ValidationItem(partName, "errorMessages", "A part Name must be entered"),
      ValidationItem(vehicle, "errorMessages", "A vehicle must be chosen from the list"))

    val modIdValue = () => modId.trim match {
      case "" => None
      case s: String => Some(s)
    }

    validate(validationChecks: _*) match {
      case Nil => {
        initialPart match {
          case Some(p) => Part.modify(p.partName.get, partName, vehicle.get, modIdValue())
          case _ => add(partName, vehicle.get, modIdValue())
        }
        S redirectTo "/app/show?entityType=Part"
      }
      case errors => {
        errors foreach (error => displayError(error._1, error._2))
        Noop
      }
    }
  }

}