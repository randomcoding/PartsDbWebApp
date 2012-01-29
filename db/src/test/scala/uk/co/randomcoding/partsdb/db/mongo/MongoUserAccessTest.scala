/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import uk.co.randomcoding.partsdb.db.util.Helpers._
import uk.co.randomcoding.partsdb.core.user.Role._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
class MongoUserAccessTest extends MongoDbTestBase {
  override val dbName = "UserAccessTest"

  lazy val access = MongoUserAccess(dbName, "") //collectionName)

  def users() = access.users

  test("Adding a single user works correctly") {
    access.addNewUser("Name", "Pass", "User") should be(None)

    users should be(Map(("Name" -> USER)))
  }

  test("Adding a multiple different users works correctly") {
    access.addNewUser("Name", "Pass", "user") should be(None)
    access.addNewUser("Name1", "Pass1", "USER") should be(None)
    access.addNewUser("Name2", "Pass2", "AdMin") should be(None)
    access.addNewUser("Name3", "Pass3", "Unknown") should be(None)

    val registeredUsers = users

    registeredUsers should have size (4)
    registeredUsers("Name") should be(USER)
    registeredUsers("Name1") should be(USER)
    registeredUsers("Name2") should be(ADMIN)
    registeredUsers("Name3") should be(USER)
  }

  test("Adding the same user multiple times does not duplicate the user") {
    access.addNewUser("Name", "Pass", "user") should be(None)
    access.addNewUser("Name", "Pass", "user") should be(Some("Cannot add user 'Name' as they already exist with role user."))
    access.addNewUser("Name", "Pass", "admin") should be(Some("Cannot add user 'Name' as they already exist with role user."))
    users should be(Map(("Name" -> USER)))
  }

  test("Modify existing user correctly changes the user") {
    access.addNewUser("Name", "Pass", "user") should be(None)
    access modifyUser ("Name", "Pass2", "admin") should be(None)
    users should be(Map(("Name" -> ADMIN)))
  }

  test("Modify User with empty database fails in expected way") {
    users should be('empty)
    access modifyUser ("NoUser", "No Pass", "No Role") should be(Some("User 'NoUser' does not exist."))
    users should be('empty)
  }

  test("Modify User that does not exist fails in expected way") {
    users should be('empty)
    access.addNewUser("Name", "Pass", "user") should be(None)
    access modifyUser ("NoUser", "No Pass", "No Role") should be(Some("User 'NoUser' does not exist."))
  }

  test("Authentication of valid user returns correct role") {
    access addNewUser ("Name", "Pass", "user") should be(None)
    access addNewUser ("Name2", "Pass2", "admin") should be(None)

    access authenticateUser ("Name", hash("Pass")) should be(Some(USER))
    access authenticateUser ("Name2", hash("Pass2")) should be(Some(ADMIN))
  }

  test("Authentication of user with empty database returns expected result") {
    users should be('empty)
    access authenticateUser ("Name", "Pass") should be(None)
  }

  test("Authentication of user that does not exist returns expected result") {
    access addNewUser ("Name", "Pass", "user") should be(None)
    access authenticateUser ("Name2", "Pass") should be(None)
  }

  test("Failed Authentication (bad password) of user returns expected result") {
    access addNewUser ("Name", "Pass", "user") should be(None)
    access authenticateUser ("Name", hash("Bad Pass")) should be(None)
  }

  test("Removal of existing user") {
    access addNewUser ("Name", "Pass", "user") should be(None)
    users should be(Map(("Name" -> USER)))

    access removeUser ("Name", "user") should be(None)
    users should be('empty)
  }

  test("Removal of non existing user from empty database") {
    users should be('empty)

    access removeUser ("Name", "user") should be(Some("Failed to remove user 'Name' with role 'user'"))
  }

  test("Removal of non existing user from non empty database") {
    users should be('empty)
    access addNewUser ("Name", "Pass", "user") should be(None)
    users should be(Map(("Name" -> USER)))

    access removeUser ("Name2", "Role2") should be(Some("Failed to remove user 'Name2' with role 'Role2'"))
  }

}