/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import uk.co.randomcoding.partsdb.core.address.{ NullAddress, AddressParser, Address }
import uk.co.randomcoding.partsdb.core.contact.{ Phone, Mobile, Email, ContactDetails }
import uk.co.randomcoding.partsdb.core.terms.PaymentTerms
import uk.co.randomcoding.partsdb.core.util.CountryCodes.countryCodes
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
import uk.co.randomcoding.partsdb.db.mongo.MongoUserAccess

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddUser extends StatefulSnippet with ErrorDisplay with DataValidation with Logger {
  val roles = List(("User" -> "user"), ("Admin" -> "admin"))

  val cameFrom = S.referer openOr "/admin/"
  var userName = ""
  var userRole = ""
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
      case Nil => addUser
      case errors => {
        errors foreach (displayError _)
        Noop
      }
    }
  }

  private[this] def addUser: JsCmd = {
    MongoUserAccess().addUser(userName, password, userRole) match {
      case None => S.redirectTo("/admin/")
      case Some(message) => {
        displayError("addUserErrorId", message)
        Noop
      }
    }
  }

  private[this] def validate = {
    // TODO move into check password functions objects
    var errors = List.empty[(String, String)]

    if (userName.trim.isEmpty) errors = ("userNameErrorId", "User Name cannot be empty") :: errors

    if (password != confirmPassword) errors = ("passwordErrorId", "Passwords do not match") :: errors

    checkPassword(password) ::: errors
  }

  private[this] def checkPassword(password: String): List[(String, String)] = {
    // TODO move into check password functions objects
    val isUpperCase = (input: String) => if (input filter (_.isLower) isEmpty) Some("Password cannot be all upper case") else None
    val isLowerCase = (input: String) => if (input filterNot (_.isLower) isEmpty) Some("Password cannot be all lower case") else None
    val longEnough = (input: String, minLength: Int) => if (input.length >= minLength) Some("Password should be at least %d characters long".format(minLength)) else None

    for {
      func <- List(isUpperCase, isLowerCase, longEnough(_: String, 6))
      result = func(password)
      if result != None
    } yield {
      ("passwordErrorId", result.get)
    }
  }
}