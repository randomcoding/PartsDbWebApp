/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import com.foursquare.rogue.Rogue._
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddressRecordTest extends MongoDbTestBase {

  override val dbName = "AddressRecordTest"

  test("Adding a single address works ok") {
    pending
  }

  test("Adding the same address more than once does not result in a duplication, or a change of underlying id") {
    pending
  }

  test("Removing an address") {
    pending
  }

  test("Removing an address from an empty database") {
    pending
  }

  test("Find an address that is present in the database") {
    // by short name, address text and oid
    pending
  }

  test("Find an address that is not present in the database") {
    // by short name, address text and oid
    pending
  }

  test("Modify an address") {
    pending
  }

  test("Modfy an address does not modify its object id") {
    pending
  }
}