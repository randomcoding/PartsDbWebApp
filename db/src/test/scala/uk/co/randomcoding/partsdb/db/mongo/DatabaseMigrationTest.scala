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
import com.foursquare.rogue.Rogue._
import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.contact.ContactDetails
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.system.SystemData
import uk.co.randomcoding.partsdb.core.user.{ User, Role }
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle
import net.liftweb.mongodb.{ MongoDB, DefaultMongoIdentifier }
import uk.co.randomcoding.partsdb.core.supplier.Supplier
import uk.co.randomcoding.partsdb.core.part.Part
import uk.co.randomcoding.partsdb.core.part.PartCost
import org.joda.time.DateTime
import uk.co.randomcoding.partsdb.core.document.LineItem
import uk.co.randomcoding.partsdb.core.document.Quote
import uk.co.randomcoding.partsdb.core.document.Document

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

  test("Migration of an unversioned database with custom User records to version 1 drops all collections but preserves the User Accounts as well as adding the bootstrap users") {
    given("A database with non default user accounts")
    val user1 = User.addUser("Alan", "654fdut43thu", Role.USER).get
    val user2 = User.addUser("Betty", "ghfdjvgfue4th3ugbrjgb", Role.ADMIN).get

    when("The migration to version 1 completes successfully")
    DatabaseMigration.migrateToVersion(1) should be(Nil)

    then("All records in the database are now removed except the Users and System Data")
    DatabaseMigration.allUsedDbCollections.filterNot(collection => collection == "users" || collection == "systemdatas") foreach (collection => recordCount(collection) should be(0))

    and("The Users collection contains the original users as well as the bootstrap users")
    recordCount("users") should be(4)
    User.fetch() should (have size (4) and
      contain(User("Dave", "freufeugregiue898", Role.USER)) and
      contain(User("Adam", "freufeugregiue898", Role.ADMIN)) and
      contain(user1) and
      contain(user2))
  }

  test("Invoking migration with a version that is less than the current version returns correct error") {
    given("A database with a version of 4")
    SystemData.databaseVersion = 4
    SystemData.databaseVersion should be(4)

    when("Migration to to version 2 is requested")
    val migrationResponse = DatabaseMigration.migrateToVersion(2)

    then("The response should indicate the new version is less than the current version")
    migrationResponse should be(List("New version (2) was less than or equal to the current version (4)"))
  }

  test("Migration to version 2 adds the C.A.T.9 Supplier") {
    given("An initial, empty database with a version of less than 2")
    DatabaseMigration.allUsedDbCollections.filterNot(_ == "users") foreach (collection => recordCount(collection) should be(0))
    SystemData.databaseVersion should be < (2)

    when("The database is successfully migrated to version 2")
    DatabaseMigration.migrateToVersion(2) should be(Nil)

    then("There is a Supplier record for C.A.T.9")
    val cat9Name = "C.A.T.9 Limited"
    val addr = Address.where(_.shortName eqs "C.A.T.9 Business Address").get.get

    val contacts = ContactDetails.create("Nicky", "01885 488 663", "", "nicky.morris@cat-9.co.uk", "01885 488 663", true)
    val cat9 = Supplier.create(cat9Name, contacts, addr, Nil)
    Supplier.where(_.supplierName eqs cat9Name).get should be(Some(cat9))
  }

  test("Migration from Version 2 to 3 with documents that have line items without suppliers correctly adds supplier ids") {
    given("A Database which is at version 2")
    SystemData.databaseVersion = 2
    SystemData.databaseVersion should be(2)

    and("Has a Vehicle, Part, Supplier and Part Cost Record")
    val vehicle = Vehicle.add(Vehicle("Vehicle 1", "")).get
    val part = Part.add("Part 1", vehicle, Some("MoD-Id")).get

    val partCost = PartCost.create(part, 10.0, new DateTime(), "S-1234")
    val supplier = Supplier.add("Supplier 1", ContactDetails("Dave", "", "", "", "", true), Address("Addr1", "Address 1", "United Kingdom"), Seq(partCost)).get

    and("A Document that has a single line item without a Supplier field")
    val lineItem = LineItem.createRecord.lineNumber(0).partId(part.id.get).quantity(1).markup(0.0).basePrice(10.0)
    val doc = Quote.add(Seq(lineItem), 10.0, "", Nil).get
    def docsToUpdate() = Document.or(_.where(_.lineItems.subfield(_.partSupplier) exists false),
      _.where(_.lineItems.subfield(_.partSupplier) eqs null)).fetch()

    docsToUpdate() should be(List(doc))

    when("The database is successfully migrated to version 3")
    DatabaseMigration.migrateToVersion(3) should be(Nil)

    then("The document will have the correct supplier id for its line item")
    val lineItems = Document.findById(doc.id.get).get.lineItems.get
    lineItems should have size (1)
    lineItems(0).partSupplier.get should be(supplier.id.get)

    and("There are no documents that do not have a supplier field for any line item")
    docsToUpdate() should be('empty)
  }
}