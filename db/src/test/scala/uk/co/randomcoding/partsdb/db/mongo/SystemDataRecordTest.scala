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
import uk.co.randomcoding.partsdb.core.system.SystemData

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class SystemDataRecordTest extends MongoDbTestBase with GivenWhenThen {
  override val dbName = "SystemDataRecordTest"

  test("Get of VAT rate returns default if there is not a record in the database") {
    given("A database with no SystemData records")
    SystemData.fetch() should be(Nil)

    when("The VAT Rate is queried")
    val vatRate = SystemData.vatRate

    then("The value is the same as the default value of 0.2")
    vatRate should be(0.2)
  }

  test("Get of Database Version returns default if there is not a record in the database") {
    given("A database with no SystemData records")
    SystemData.fetch() should be(Nil)

    when("The Database Version is queried")
    val dbVersion = SystemData.databaseVersion

    then("The value is the same as the default value of -1")
    dbVersion should be(-1)
  }

  test("Setting the VAT Rate adds a System Data record to the database with the correct VAT Rate value") {
    given("A database with no SystemData records")
    SystemData.fetch() should be(Nil)

    when("The VAT Rate is set to 0.3")
    SystemData.vatRate = 0.3

    then("There is now a single SystemData record in the database")
    SystemData.fetch() should have size (1)

    and("The VAT rate is now 0.3")
    SystemData.vatRate should be(0.3)
  }

  test("Setting the Database Version adds a System Data record to the database with the correct Database Version value") {
    given("A database with no SystemData records")
    SystemData.fetch() should be(Nil)

    when("The Database Version is set to 3")
    SystemData.databaseVersion = 3

    then("There is now a single SystemData record in the database")
    SystemData.fetch() should have size (1)

    and("The VAT rate is now 0.3")
    SystemData.databaseVersion should be(3)
  }

  test("Setting the Database Version multiple times results in only a single SystemData record in the database with the correct Database Version value") {
    given("A database with no SystemData records")
    SystemData.fetch() should be(Nil)

    when("The database version is set to 3")
    SystemData.databaseVersion = 3
    and("Then it is set to 4")
    SystemData.databaseVersion = 4
    and("Then it is set to 5")
    SystemData.databaseVersion = 5
    and("Then it is set to 6")
    SystemData.databaseVersion = 6

    then("The Database Version is 6")
    SystemData.databaseVersion should be(6)

    and("There is only a single SystemData record in the database")
    SystemData.fetch() should have size (1)
  }
}