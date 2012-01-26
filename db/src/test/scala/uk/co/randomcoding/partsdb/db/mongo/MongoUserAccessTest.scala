/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

import uk.co.randomcoding.partsdb.db.util.Helpers._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
class MongoUserAccessTest extends MongoDbTestBase {
  override val dbName = "UserAccessTest"

  lazy val access = MongoUserAccess(dbName, "") //collectionName)

  def users = access.users

  test("Adding a single user works correctly") {
    access.addUser("Name", "Pass", "Role") should be(None)
    users should be(List(("Name", "Role")))
  }

  test("Adding a multiple different users works correctly") {
    access.addUser("Name", "Pass", "Role") should be(None)
    access.addUser("Name1", "Pass1", "Role1") should be(None)
    access.addUser("Name2", "Pass2", "Role2") should be(None)

    users should (have size (3) and
      contain(("Name", "Role")) and
      contain(("Name1", "Role1")) and
      contain(("Name2", "Role2")))
  }

  test("Adding the same user multiple times does not duplicate the user") {
    access.addUser("Name", "Pass", "Role") should be(None)
    access.addUser("Name", "Pass", "Role") should be(Some("User 'Name' already exists. Please use modifyUser instead"))
    users should be(List(("Name", "Role")))
  }

  test("Modify existing user correctly changes the user") {
    access.addUser("Name", "Pass", "Role") should be(None)
    access modifyUser ("Name", "Pass2", "Role2") should be(None)
    users should be(List(("Name", "Role2")))
  }

  test("Modify User with empty database fails in expected way") {
    users should be('empty)
    access modifyUser ("NoUser", "No Pass", "No Role") should be(Some("User 'NoUser' does not exist. Please use addUser instead"))
    users should be('empty)
  }

  test("Modify User that does not exist fails in expected way") {
    users should be('empty)
    access.addUser("Name", "Pass", "Role") should be(None)
    access modifyUser ("NoUser", "No Pass", "No Role") should be(Some("User 'NoUser' does not exist. Please use addUser instead"))
  }

  test("Authentication of valid user returns correct role") {
    access.addUser("Name", "Pass", "Role") should be(None)
    access userRole ("Name", hash("Pass")) should be(Some("Role"))
  }

  test("Authentication of user with empty database returns expected result") {
    users should be('empty)
    access userRole ("Name", "Pass") should be(None)
  }

  test("Authentication of user that does not exist returns expected result") {
    access.addUser("Name", "Pass", "Role") should be(None)
    access userRole ("Name2", "Pass") should be(None)
  }

  test("Failed Authentication (bad password) of user returns expected result") {
    access.addUser("Name", "Pass", "Role") should be(None)
    access userRole ("Name", hash("Bad Pass")) should be(None)
  }

  test("Removal of existing user") {
    access addUser ("Name", "Pass", "Role") should be(None)
    users should be(List(("Name", "Role")))

    access removeUser ("Name", "Role") should be(None)
    users should be('empty)
  }

  test("Removal of non existing user from empty database") {
    users should be('empty)

    access removeUser ("Name", "Role") should be(Some("User 'Name' with role 'Role' does not exist and cannot be removed"))
  }

  test("Removal of non existing user from non empty database") {
    users should be('empty)
    access addUser ("Name", "Pass", "Role") should be(None)
    users should be(List(("Name", "Role")))

    access removeUser ("Name2", "Role2") should be(Some("User 'Name2' with role 'Role2' does not exist and cannot be removed"))
  }

}