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
import uk.co.randomcoding.partsdb.db.mongo.MongoAllOrOneAccess
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle
import uk.co.randomcoding.partsdb.core.part.PartCost

class AddPart extends StatefulSnippet with DbAccessSnippet with ErrorDisplay with DataValidation with Logger {

  val cameFrom = S.referer openOr "/app/show?entityType=Part"
  var partName = ""
  var modId = ""

  var vehicle: Option[Vehicle] = None
  var vehicles: Option[List[Vehicle]] = None
  val allVehicles = getAllVehicles.map(v => (Some(v), v.vehicleName))
  var vehicleList: List[Vehicle] = Nil

  //  val allVehicles = getAllVehicles().map(v => (Some(v), v.vehicleName))
  //         "#vehicleEntry" #> styledObjectSelect[Option[Vehicle]](allVehicles, vehicle, (v: Option[Vehicle]) => vehicle = v) & 

  def dispatch = {
    case "render" => render
  }

  def render = {
    "#formTitle" #> Text("Add Part") &
      "#nameEntry" #> styledText(partName, partName = _) &
      "#modIdEntry" #> styledText(modId, modId = _) &
      "#vehicleEntry" #> styledObjectSelect[Option[Vehicle]](allVehicles, vehicle, (v: Option[Vehicle]) => vehicle = v) &
      "#vehicleSubmit" #> button("Vehicle Submit", processVehicleSubmit) &
      "#submit" #> button("Submit", processSubmit)
  }

  /**
   * Method called when the vehicle submit button is pressed.
   *
   * This adds a vehicle to the list of vehicles.
   */
  private[this] def processVehicleSubmit() = {
    // vehicleList.+:(vehicle)
    vehicleList.+:(vehicle)
  }

  /**
   * Method called when the submit button is pressed.
   *
   * This extracts the details required to make the Part object and if they validate, adds them to the database.
   *
   * On successful addition, this will (possibly display a dialogue and then) redirect to the main customers page
   */
  private[this] def processSubmit() = {

    //    var vehicles = List[Vehicle]()
    //    vehicles = vehicle ::: List(vehicle)
    /*    if (!vehicleList.isEmpty)
    {
      var vehicles: Option[List[Vehicle]] = Option[List[Vehicle]](vehicleList)     
    }*/

    val validationChecks = Seq(
      ValidationItem(partName, "partNameError", "Part Name must be entered"),
      //      ValidationItem(partCosts, "partCostError", "Part Cost is not valid"),
      ValidationItem(vehicle, "partVehicleError", "Vehicle is not valid"))
    // ValidationItem(modId, "modIdError", "MoD ID must be entered"))

    validate(validationChecks: _*) match {
      case Nil => {
        //        addNewPart(partName, cost, vehicle.get)
        //Part(val partId: Identifier, val partName: String, val vehicles: Option[List[Vehicle]] = None, val modId: Option[String] = None)

        addNewPart(partName, vehicleList, modId)
        S redirectTo "/app/show?entityType=" + "Part"
      }
      case errors => {
        errors foreach (error => displayError(error._1, error._2))
        Noop
      }
    }
  }

}