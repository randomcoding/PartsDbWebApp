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
package uk.co.randomcoding.partsdb.db.mongo

import org.scalatest.GivenWhenThen
import uk.co.randomcoding.partsdb.core.user.User
import uk.co.randomcoding.partsdb.core.user.Role._
import uk.co.randomcoding.partsdb.db.util.Helpers.{ hash => pwhash }

/**
 * @author RandomCoder
 */
class UserRecordTest extends MongoDbTestBase with GivenWhenThen {

  val dbName = "UserRecordTest"

  test("A user can be created and modified and still be able to login - Issue 146") {
    given("a database with a single user that can be authenticated")
    val userName = "User123"
    val originalPass = pwhash("pass1234")
    User.addUser(User(userName, originalPass, USER))
    User.authenticate(userName, originalPass) should be(Some(User(userName, originalPass, USER)))

    when("the User's password is modified")
    val newPass = pwhash("newpass123")
    User.modify(userName, userName, newPass, USER) should be(Some(User(userName, newPass, USER)))

    then("the user can be authenticated with the new password")
    User.authenticate(userName, newPass) should be(Some(User(userName, newPass, USER)))

    and("not authenticated with the old password")
    User.authenticate(userName, originalPass) should be(None)
  }

  test("Adding a single user works correctly") {
    User.addUser(User("Name", "Pass", USER)) should be(Some(User("Name", "Pass", USER)))

    users should be(Map(("Name" -> USER)))
  }

  test("Adding user with unrecognised role fails with the expected message") {
    User.addUser(User("Name3", "Pass3", "Unknown")) should be(None)
    User.addUser(User("Name4", "Pass4", "")) should be(None)

  }

  test("Adding a multiple different users works correctly") {
    User.addUser(User("Name", "Pass", "user")) should be(Some(User("Name", "Pass", USER)))
    User.addUser(User("Name1", "Pass1", "USER")) should be(Some(User("Name1", "Pass1", USER)))
    User.addUser(User("Name2", "Pass2", "AdMin")) should be(Some(User("Name2", "Pass2", ADMIN)))

    val registeredUsers = users

    registeredUsers should (have size (3) and
      contain(("Name" -> USER)) and
      contain(("Name1" -> USER)) and
      contain(("Name2" -> ADMIN)))
  }

  test("Adding the same user multiple times does not duplicate the user") {
    User.addUser(User("Name", "Pass", "user")) should be(Some(User("Name", "Pass", USER)))
    users should be(Map(("Name" -> USER)))
    User.addUser(User("Name", "Pass", "user")) should be(None)
    users should be(Map(("Name" -> USER)))
    User.addUser(User("Name", "Pass", "admin")) should be(None)
    users should be(Map(("Name" -> USER)))
  }

  ignore("Modify role of existing user correctly changes the user. - This currently fails to update the role (Issue 149)") {
    User.addUser(User("Name", "Pass", "user")) should be(Some(User("Name", "Pass", USER)))
    User.modify("Name", "Name", "Pass2", "admin") should be(Some(User("Name", "Pass2", ADMIN)))
    users should be(Map(("Name" -> ADMIN)))
  }

  test("Modify name of existing user correctly changes the user") {
    User.addUser(User("Name", "Pass", "user")) should be(Some(User("Name", "Pass", USER)))
    User.modify("Name", "Name2", "Pass", USER) should be(Some(User("Name2", "Pass", USER)))
    users should be(Map(("Name2" -> USER)))
  }

  test("Modify User with empty database fails in expected way") {
    users should be('empty)
    User.modify("NoUser", "NoUser", "No Pass", "No Role") should be(None)
    users should be('empty)
  }

  test("Modify User that does not exist fails in expected way") {
    users should be('empty)
    User.addUser(User("Name", "Pass", "user")) should be(Some(User("Name", "Pass", USER)))
    User.modify("NoUser", "NoUser", "No Pass", "No Role") should be(None)
    users should be(Map("Name" -> USER))
  }

  test("Authentication of valid user returns correct role") {
    User.addUser(User("Name", pwhash("Pass"), "user")) should be(Some(User("Name", "Pass", USER)))
    User.addUser(User("Name2", pwhash("Pass2"), "admin")) should be(Some(User("Name2", "Pass2", ADMIN)))

    User.authenticate("Name", pwhash("Pass")) should be(Some(User("Name", pwhash("Pass"), USER)))
    User.authenticate("Name2", pwhash("Pass2")) should be(Some(User("Name2", pwhash("Pass2"), ADMIN)))
  }

  test("Authentication of user with empty database returns expected result") {
    users should be('empty)
    User.authenticate("Name", "Pass") should be(None)
  }

  test("Authentication of user that does not exist returns expected result") {
    User.addUser(User("Name", "Pass", "user")) should be(Some(User("Name", "Pass", USER)))
    User.authenticate("Name2", "Pass") should be(None)
  }

  test("Failed Authentication (bad password) of user returns expected result") {
    User.addUser(User("Name", "Pass", "user")) should be(Some(User("Name", "Pass", USER)))
    User.authenticate("Name", pwhash("Bad Pass")) should be(None)
  }

  test("Removal of existing user") {
    val user = User("Name", "Pass", "user")
    User.addUser(user) should be(Some(User("Name", "Pass", USER)))
    users should be(Map(("Name" -> USER)))

    User.remove(user) should be(true)
    users should be('empty)
  }

  test("Removal of non existing user from empty database") {
    users should be('empty)

    User.remove(User("Name", "Pass", "user")) should be(false)
    users should be('empty)
  }

  test("Removal of non existing user from non empty database") {
    users should be('empty)
    User.addUser(User("Name", "Pass", "user")) should be(Some(User("Name", "Pass", USER)))
    users should be(Map(("Name" -> USER)))

    User.remove(User("Name1", "Pass", "user")) should be(false)
    users should be(Map(("Name" -> USER)))
  }

  private[this] def users: Map[String, Role] = User.findAll map (user => (user.username.get, user.role.get)) toMap

}