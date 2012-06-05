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
import uk.co.randomcoding.partsdb.core.system.SystemData
import net.liftweb.mongodb.{ MongoDB, DefaultMongoIdentifier }
import com.foursquare.rogue.Rogue._
import uk.co.randomcoding.partsdb.core.user.User
import uk.co.randomcoding.partsdb.core.user.Role
import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.contact.ContactDetails

/**
 * This should contain tests for each migration version
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class DatabaseMigrationTest extends MongoDbTestBase with GivenWhenThen {
  override val dbName = "DatabaseMigrationTest"

  private[this] def recordCount(collectionName: String): Long = {
    MongoDB.getDb(DefaultMongoIdentifier) match {
      case Some(db) => db.getCollection(collectionName).count
      case _ => {
        fail("Unable to load Default Mongo Database for collection %s!".format(collectionName))
        -1
      }
    }
  }

  test("Migration of unversioned empty database to version 1 successfully drops all collections and sets the database version to 1") {
    given("An Empty Database without a System Data record")
    DatabaseMigration.allUsedDbCollections.filterNot(_ == "users") foreach (collection => recordCount(collection) should be(0))

    when("The Migration to version 1 is executed successfully")
    DatabaseMigration.migrateToVersion(1) should be(Nil)

    then("The database is still empty apart from the users and System Data")
    DatabaseMigration.allUsedDbCollections.filterNot(coll => (coll == "users" || coll == "systemdatas")) foreach (collection => recordCount(collection) should be(0))

    and("There should be a single record in the System Data collection")
    recordCount("systemdatas") should be(1)

    and("There should be a two records in the Users collection")
    recordCount("users") should be(2)
    User.fetch() should (have size (2) and
      contain(User("Dave", "freufeugregiue898", Role.USER)) and
      contain(User("Adam", "freufeugregiue898", Role.ADMIN)))

    and("The database version is 1")
    SystemData.databaseVersion should be(1)
  }

  test("Migration of unversioned database with data in some tables to version 1 successfully drops all collections and sets the database version to 1") {
    given("An Unversioned database with some data in a few different collections")
    Address.add("Addr1", "Address 1", "UK")
    Vehicle.add(Vehicle("Vehicle 1", "vehicle.pdf"))
    Customer.add("Customer 1", Address.add("Addr2", "Address 2", "UK").get, 30, ContactDetails.create("Dave", "09", "08", "e@m.l", "03", true))

    Address.fetch should (have size (2) and
      contain(Address("Addr1", "Address 1", "UK")) and
      contain(Address("Addr2", "Address 2", "UK")))

    Vehicle.fetch() should be(List(Vehicle("Vehicle 1", "vehicle.pdf")))

    recordCount("customers") should be(1)

    val excluded = List("users", "vehicles", "addresss", "customers")
    DatabaseMigration.allUsedDbCollections.filterNot(excluded.contains(_)) foreach (collection => recordCount(collection) should be(0))

    when("The migration to version 1 is executed successfully")
    DatabaseMigration.migrateToVersion(1) should be(Nil)

    then("All records in the database are now removed except the Users and System Data")
    DatabaseMigration.allUsedDbCollections.filterNot(collection => collection == "users" || collection == "systemdatas") foreach (collection => recordCount(collection) should be(0))

    and("The database version is 1")
    SystemData.databaseVersion should be(1)

    and("There should be a single record in the System Data collection")
    recordCount("systemdatas") should be(1)

    and("There should be a two records in the Users collection")
    recordCount("users") should be(2)
    User.fetch() should (have size (2) and
      contain(User("Dave", "freufeugregiue898", Role.USER)) and
      contain(User("Adam", "freufeugregiue898", Role.ADMIN)))
  }
}