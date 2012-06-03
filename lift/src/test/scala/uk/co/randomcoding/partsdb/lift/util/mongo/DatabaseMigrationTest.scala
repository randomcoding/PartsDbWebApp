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
package uk.co.randomcoding.partsdb.lift.util.mongo

import uk.co.randomcoding.partsdb.db.mongo.MongoDbTestBase

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class DatabaseMigrationTest extends MongoDbTestBase {
  override val dbName = "DatabaseMigrationTest"

  test("Migration of unversioned empty database to version 1 successfully drops all collections") {
    pending
  }

  test("Migration of unversioned database with data in some tables to version 1 successfully drops all collections") {
    pending
  }
}