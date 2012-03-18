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

import net.liftweb.common.{ Logger, Full }
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.http.{ StatefulSnippet, S }
import net.liftweb.util.Helpers._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddEditUser extends StatefulSnippet with ErrorDisplay with DataValidation with SubmitAndCancelSnippet with Logger {
  private[this] val roles = List(("User" -> "User"), ("Admin" -> "Admin"))

  private[this] val initialUser = S param ("id") match {
    case Full(id) => User findById (new ObjectId(id))
    case _ => None
  }

  override val cameFrom = S.referer openOr "/admin/"

  private[this] var (userName, userRole) = initialUser match {
    case Some(u) => (u.username.get, u.role.get.toString)
    case _ => ("", "")
  }

  private[this] var password = ""
  private[this] var confirmPassword = ""

  def dispatch = {
    case "render" => render
  }

  def render = {
    "#formTitle" #> Text("Add User") &
      "#nameEntry" #> styledText(userName, userName = _) &
      "#userRoleEntry" #> styledSelect(roles, "User", userRole = _) &
      "#passwordEntry" #> styledPassword(password, password = _) &
      "#confirmPasswordEntry" #> styledPassword(confirmPassword, confirmPassword = _) &
      renderSubmitAndCancel()
  }

  /**
   * Method called when the submit button is pressed.
   *
   * This will check the user name is not empty, that the two passwords match and that basic checks are done on the password
   * (not all same case and longer than 6 characters)
   */
  override def processSubmit() = {
    performValidation(passwordValidates) match {
      case Nil => addOrUpdateUser
      case errors => {
        displayErrors(errors: _*)
        Noop
      }
    }
  }

  private[this] def addOrUpdateUser = initialUser match {
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
        displayError(message)
        Noop
      }
    }
  }

  override val validationItems: Seq[ValidationItem] = Seq(ValidationItem(userName, "User Name"))

  private[this] val passwordMinLength = 6
  private[this] val passwordValidates = () => passwordErrors(password, confirmPassword, passwordMinLength)
}
