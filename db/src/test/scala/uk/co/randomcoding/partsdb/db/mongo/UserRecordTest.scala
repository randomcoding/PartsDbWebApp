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
    User.addUser(userName, originalPass, USER)
    User.authenticate(userName, originalPass) should be(Some(User(userName, originalPass, USER)))

    when("the User's password is modified")
    val newPass = pwhash("newpass123")
    User.modify(userName, userName, newPass, USER)

    then("the user can be authenticated with the new password")
    User.authenticate(userName, newPass) should be(Some(User(userName, newPass, USER)))

    and("not authenticated with the old password")
    User.authenticate(userName, originalPass) should be(None)
  }

}