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

import uk.co.randomcoding.partsdb.core.part.PartKit
import uk.co.randomcoding.partsdb.core.part.Part
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle
import uk.co.randomcoding.partsdb.core.document.LineItem

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class PartKitRecordTest extends MongoDbTestBase {

  override val dbName = "PartKitTest"

  private[this] val part1 = Part.create("Part", Vehicle.create("Vehicle"), Some("modId"))

  test("Hash Code and Equality for Part Kits That Should be Equals") {
    val emptyKit1 = PartKit("empty kit", Nil)
    val emptyKit2 = PartKit("empty kit", Nil)
    val emptyKit3 = PartKit("empty kit", Nil)

    emptyKit1 should (equal(emptyKit2) and equal(emptyKit3))
    emptyKit2 should (equal(emptyKit1) and equal(emptyKit3))
    emptyKit3 should (equal(emptyKit1) and equal(emptyKit2))

    emptyKit1.hashCode should (equal(emptyKit2.hashCode) and equal(emptyKit3.hashCode))

    val kit1 = PartKit("Kit", Seq(LineItem.create(0, part1, 1, 100, 0)))
    val kit2 = PartKit("Kit", Seq(LineItem.create(0, part1, 1, 100, 0)))
    val kit3 = PartKit("Kit", Seq(LineItem.create(0, part1, 1, 100, 0)))

    kit1 should (equal(kit2) and equal(kit3))
    kit2 should (equal(kit1) and equal(kit3))
    kit3 should (equal(kit1) and equal(kit2))

    kit1.hashCode should (equal(kit2.hashCode) and equal(kit3.hashCode))
  }

  test("Hash Code and Equality for Part Kits That Should not be Equals") {
    val emptyKit1 = PartKit("empty kit 1", Nil)
    val emptyKit2 = PartKit("empty kit 2", Nil)
    val emptyKit3 = PartKit("empty kit 3", Nil)

    emptyKit1 should (not equal (emptyKit2) and not equal (emptyKit3))
    emptyKit2 should (not equal (emptyKit1) and not equal (emptyKit3))
    emptyKit3 should (not equal (emptyKit1) and not equal (emptyKit2))

    emptyKit1.hashCode should (not equal (emptyKit2.hashCode) and not equal (emptyKit3.hashCode))

    val kit1 = PartKit("Kit", Seq(LineItem.create(0, part1, 1, 100, 0)))
    val kit2 = PartKit("Kit", Seq(LineItem.create(0, part1, 2, 100, 0)))
    val kit3 = PartKit("Kit", Seq(LineItem.create(0, part1, 1, 200, 0)))

    kit1 should (not equal (kit2) and not equal (kit3))
    kit2 should (not equal (kit1) and not equal (kit3))
    kit3 should (not equal (kit1) and not equal (kit2))

    kit1.hashCode should (not equal (kit2.hashCode) and not equal (kit3.hashCode))
  }

}