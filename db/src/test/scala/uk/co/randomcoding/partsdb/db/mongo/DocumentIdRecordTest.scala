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

import uk.co.randomcoding.partsdb.core.document.DocumentId
import com.foursquare.rogue.Rogue._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class DocumentIdRecordTest extends MongoDbTestBase {
  override val dbName = "DocumentIdRecordTest"

  test("First Call to next id returns id 1") {
    DocumentId.nextId.currentId.get should be(1l)
  }

  test("Sequential calls to next id return the correct sequence of numbers") {
    (1 to 10000) foreach (index => DocumentId.nextId.currentId.get should be(index))
  }

  test("Multiple calls to next id do not create multiple records in the database") {
    DocumentId.nextId.currentId.get should be(1l)
    (DocumentId fetch) should be(List(DocumentId.createRecord.currentId(1)))
    DocumentId.nextId.currentId.get should be(2l)
    (DocumentId fetch) should be(List(DocumentId.createRecord.currentId(2)))
    DocumentId.nextId.currentId.get should be(3l)
    (DocumentId fetch) should be(List(DocumentId.createRecord.currentId(3)))
    DocumentId.nextId.currentId.get should be(4l)
    (DocumentId fetch) should be(List(DocumentId.createRecord.currentId(4)))
  }
}
