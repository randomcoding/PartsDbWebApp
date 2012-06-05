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

/**
 * This should contain tests for each migration version
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class DatabaseMigrationTest extends MongoDbTestBase with GivenWhenThen {
  override val dbName = "DatabaseMigrationTest"

  private[this] def recordCount(collectionName: String): Long = {
    MongoDB.getDb(DefaultMongoIdentifier) match {
      case Some(db) => {
        db.getCollection(collectionName).count
      }
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

    and("The database version is 1")
    SystemData.databaseVersion should be(1)
  }

  test("Migration of unversioned database with data in some tables to version 1 successfully drops all collections and sets the database version to 1") {
    given("An Unversioned database with some data in a few different collections")

    when("The migration to version 1 is executed successfully")

    then("All records in the database are now removed except the Users and System Data")

    and("The database version is 1")
    //SystemData.databaseVersion should be(1)
    pending
  }
}