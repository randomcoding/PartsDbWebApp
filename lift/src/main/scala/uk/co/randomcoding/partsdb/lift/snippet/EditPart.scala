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

class EditPart extends StatefulSnippet with DbAccessSnippet with ErrorDisplay with DataValidation with Logger {
  val cameFrom = S.referer openOr "/parts"
  var name = ""
  var cost = ""

  def dispatch = {
    case "render" => render
  }

  def render = {
    "#formTitle" #> Text("Edit Part") &
      "#nameEntry" #> styledText(name, name = _) &
      "#costEntry" #> styledText(cost, cost = _) &
      "#submit" #> button("Submit", processSubmit)
  }

  /**
   * Method called when the submit button is pressed.
   *
   * This extracts the details required to make the Part object and if they validate, adds them to the database.
   *
   * On successful addition, this will (possibly display a dialogue and then) redirect to the main parts page
   */
  private[this] def processSubmit() = {
    val validationChecks = Seq(ValidationItem(name, "partNameError", "Part Name must be entered"),
      ValidationItem(cost, "partCostError", "Part Cost is not valid"))

    validate(validationChecks: _*) match {
      case Nil => {
        val newId = editPart(name, cost.toDouble).partId
        S redirectTo "/parts?highlight=%d".format(newId.id)
      }
      case errors => {
        errors foreach (error => displayError(error._1, error._2))
        // TODO: Need to ensure that the entered details are still present
        Noop
      }
    }
  }
}