/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import scala.xml.Text

import org.bson.types.ObjectId

import uk.co.randomcoding.partsdb.core.user.Role.stringToRole
import uk.co.randomcoding.partsdb.core.user.User
import uk.co.randomcoding.partsdb.db.mongo.MongoUserAccess._
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.auth.PasswordValidation.passwordErrors
import uk.co.randomcoding.partsdb.lift.util.snippet._

import net.liftweb.common.StringOrNodeSeq.strTo
import net.liftweb.common.{Logger, Full}
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.http.{StatefulSnippet, S}
import net.liftweb.util.Helpers._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddEditUser extends StatefulSnippet with ErrorDisplay with DataValidation with Logger {
  val roles = List(("User" -> "User"), ("Admin" -> "Admin"))

  val initialUser = S param ("id") match {
    case Full(id) => User findById (new ObjectId(id))
    case _ => None
  }

  val cameFrom = S.referer openOr "/admin/"
  var (userName, userRole) = initialUser match {
    case Some(u) => (u.username.get, u.role.get.toString)
    case _ => ("", "")
  }
  var password = ""
  var confirmPassword = ""

  def dispatch = {
    case "render" => render
  }

  def render = {
    "#formTitle" #> Text("Add User") &
      "#nameEntry" #> styledText(userName, userName = _) &
      "#userRoleEntry" #> styledSelect(roles, "User", userRole = _) &
      "#passwordEntry" #> styledPassword(password, password = _) &
      "#confirmPasswordEntry" #> styledPassword(confirmPassword, confirmPassword = _) &
      "#submit" #> button("Submit", processSubmit)
  }

  /**
   * Method called when the submit button is pressed.
   *
   * This will check the user name is not empty, that the two passwords match and that basic checks are done on the password
   * (not all same case and longer than 6 characters)
   */
  private[this] def processSubmit() = {
    validate match {
      case Nil => initialUser match {
        case Some(u) => {
          User.modify(u.username.get, userName, password match {
            case "" => u.password.get
            case p => hash(p)
          }, userRole)
          S.redirectTo("/admin/")
        }
        case _ => addNewUser(userName, password, userRole) match {
          case None => S.redirectTo("/admin/")
          case Some(message) => {
            //displayError("addUserErrorId", message)
            displayError("errorMessages", message)
            Noop
          }
        }
      }
      case errors => {
        displayError(errors: _*)
        Noop
      }
    }
  }

  private[this] def validate = {
    var errors = List.empty[(String, String)]

    // if (userName.trim.isEmpty) errors = ("userNameErrorId", "User Name cannot be empty") :: errors
    if (userName.trim.isEmpty) errors = ("errorMessages", "User Name cannot be empty") :: errors

    //val pwErrorId = "passwordErrorId"
    val pwErrorId = "errorMessages"
    val validationErrors = passwordErrors(password, confirmPassword, 6) map ((pwErrorId, _))
    validationErrors ::: errors
  }
}
