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
