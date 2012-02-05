/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.auth

import uk.co.randomcoding.partsdb.db.mongo.MongoDbTestBase

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AppAuthenticationTest extends MongoDbTestBase {
  override val dbName = "AuthTest"

  test("Valid user credentials authenticate") {
    pending
  }

  test("Invalid User Cresentials Fail to Validate") {
    pending
  }

}