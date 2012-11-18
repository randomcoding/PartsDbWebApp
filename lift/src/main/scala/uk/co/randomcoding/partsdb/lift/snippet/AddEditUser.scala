/*
 * Copyright (C) 2012 RandomCoder <randomcoder@randomcoding.co.uk>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *    RandomCoder - initial API and implementation and/or initial documentation
 */
package uk.co.randomcoding.partsdb.lift.snippet

import scala.xml.Text
import uk.co.randomcoding.partsdb.core.user.Role.stringToRole
import uk.co.randomcoding.partsdb.core.user.User
import uk.co.randomcoding.partsdb.core.util.MongoHelpers._
import uk.co.randomcoding.partsdb.db.mongo.MongoUserAccess._
import uk.co.randomcoding.partsdb.db.util.Helpers.{ hash => pwhash }
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.auth.PasswordValidation.passwordErrors
import uk.co.randomcoding.partsdb.lift.util.snippet._
import net.liftweb.common.{ Logger, Full }
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.http.{ StatefulSnippet, S }
import net.liftweb.util.Helpers._
import net.liftweb.http.js.jquery.JqJsCmds
import net.liftweb.http.SHtml.ElemAttr._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddEditUser extends StatefulSnippet with ErrorDisplay with DataValidation with SubmitAndCancelSnippet with Logger {
  private[this] val roles = List(("User" -> "User"), ("Admin" -> "Admin"))

  private[this] val initialUser = S param ("id") match {
    case Full(id) => User findById id
    case _ => None
  }

  override val cameFrom = () => "/admin/"

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
      "#userRoleEntry" #> (initialUser match {
        case Some(user) => styledSelect(roles, "User", userRole = _, List(("disabled", "disabled"))) ++ Text("  Please remove and re-add this user to change their role")
        case _ => styledSelect(roles, "User", userRole = _)
      }) &
      "#passwordEntry" #> styledPassword(password, password = _) &
      "#confirmPasswordEntry" #> styledPassword(confirmPassword, confirmPassword = _) &
      "#removeUser" #> (initialUser match {
        case Some(u) => styledButton("Remove User", removeUser)
        case _ => Text("")
      }) &
      renderSubmitAndCancel()
  }

  private[this] val removeUser = () => {
    User.remove(initialUser.get) match {
      case true => // Do nothing
      case false => displayError("Failed to  Remove User: %s".format(initialUser.get.username.get))
    }

    S.redirectTo("/admin/")
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
      debug("Update User: %s".format(u))
      val hashPw = password match {
        case "" => u.password.get // Password is not updated
        case p => pwhash(p) // password is updated
      }
      User.modify(u.username.get, userName, hashPw, userRole)
      S.redirectTo("/admin/")
    }
    case _ => User.addUser(User(userName, pwhash(password), userRole)) match {
      case Some(user) => S.redirectTo("/admin/")
      case _ => {
        displayError("Failed to add new user with name %s and role %s".format(userName, userRole))
        Noop
      }
    }
  }

  override def validationItems(): Seq[ValidationItem] = Seq(ValidationItem(userName, "User Name"))

  private[this] val passwordMinLength = 6
  private[this] val passwordValidates = () => passwordErrors(password, confirmPassword, passwordMinLength)
}
