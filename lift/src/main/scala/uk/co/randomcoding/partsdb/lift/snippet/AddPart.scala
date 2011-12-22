package uk.co.randomcoding.partsdb.lift.snippet

/**
 * @author Jane Rowe
 */

import uk.co.randomcoding.partsdb.core.part.Part
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet.{ ValidationItem, ErrorDisplay, DbAccessSnippet, DataValidation, StyleAttributes }
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
import uk.co.randomcoding.partsdb.core.vehicle.{ Vehicle, DefaultVehicle }
import uk.co.randomcoding.partsdb.db.mongo.MongoAllOrOneAccess

class AddPart extends StatefulSnippet with DbAccessSnippet with ErrorDisplay with DataValidation with Logger {

  val cameFrom = S.referer openOr "/app/show?entityType=part"
  var partName = ""
  var costText = ""
  //val defaultVehicle: Vehicle = DefaultVehicle

  val vehicles = getAllVehicles()
  val vehicleList = vehicles.map(v => (v, v.vehicleName))
  var vehicle = vehicles.head

  def dispatch = {
    case "render" => render
  }

  def render = {
    "#formTitle" #> Text("Add Part") &
      "#nameEntry" #> styledText(partName, partName = _) &
      "#costEntry" #> styledText(costText, costText = _) &
      "#vehicleEntry" #> styledSelectObject[Vehicle](vehicleList, vehicle, vehicle = _) &
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

    val cost: java.lang.Double = asDouble(costText) match {
      case _ => costText.toDouble
    }

    val validationChecks = Seq(
      ValidationItem(partName, "partNameError", "Part Name must be entered"),
      ValidationItem(cost, "partCostError", "Part Cost is not valid"),
      ValidationItem(vehicle, "partVehicleError", "Vehicle is not valid"))

    validate(validationChecks: _*) match {
      case Nil => {
        val newId = addNewPart(partName, cost, vehicle).partId
        S redirectTo "/app/show?entityType=part".format(newId.id)
      }
      case errors => {
        errors foreach (error => displayError(error._1, error._2))
        Noop
      }
    }
  }

}