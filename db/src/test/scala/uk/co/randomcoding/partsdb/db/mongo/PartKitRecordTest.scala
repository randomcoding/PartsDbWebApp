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

import com.foursquare.rogue.Rogue._
import uk.co.randomcoding.partsdb.core.document.LineItem
import uk.co.randomcoding.partsdb.core.part.{ PartKit, Part }
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle
import uk.co.randomcoding.partsdb.core.supplier.Supplier
import uk.co.randomcoding.partsdb.core.contact.ContactDetails
import uk.co.randomcoding.partsdb.core.address.Address

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class PartKitRecordTest extends MongoDbTestBase {

  override val dbName = "PartKitTest"

  private[this] val part1 = Part.create("Part", Vehicle.create("Vehicle"), Some("modId"))
  private[this] val supplier1 = Supplier("Supplier", ContactDetails("Dave", "", "", "", "", true), Address("Addr1", "Address 1", "UK"), Nil)

  private[this] def line(lineNumber: Int = 0, part: Part = part1, quantity: Int = 1, price: Double = 100.0d, markup: Double = 0.0d, supplier: Supplier = supplier1): LineItem = {
    LineItem.create(lineNumber, part, quantity, price, markup, supplier)
  }

  test("Hash Code and Equality for Part Kits That Should be Equals") {
    val emptyKit1 = PartKit("empty kit", Nil)
    val emptyKit2 = PartKit("empty kit", Nil)
    val emptyKit3 = PartKit("empty kit", Nil)

    emptyKit1 should (equal(emptyKit2) and equal(emptyKit3))
    emptyKit2 should (equal(emptyKit1) and equal(emptyKit3))
    emptyKit3 should (equal(emptyKit1) and equal(emptyKit2))

    emptyKit1.hashCode should (equal(emptyKit2.hashCode) and equal(emptyKit3.hashCode))

    val kit1 = PartKit("Kit", Seq( /*LineItem.create(0, part1, 1, 100, 0, supplier)*/ line()))
    val kit2 = PartKit("Kit", Seq( /*LineItem.create(0, part1, 1, 100, 0, supplier)*/ line()))
    val kit3 = PartKit("Kit", Seq( /*LineItem.create(0, part1, 1, 100, 0, supplier)*/ line()))

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

    val kit1 = PartKit("Kit", Seq( /*LineItem.create(0, part1, 1, 100, 0)*/ line()))
    val kit2 = PartKit("Kit", Seq( /*LineItem.create(0, part1, 2, 100, 0))*/ line(quantity = 2)))
    val kit3 = PartKit("Kit", Seq( /*LineItem.create(0, part1, 1, 200, 0))*/ line(price = 200d)))

    kit1 should (not equal (kit2) and not equal (kit3))
    kit2 should (not equal (kit1) and not equal (kit3))
    kit3 should (not equal (kit1) and not equal (kit2))

    kit1.hashCode should (not equal (kit2.hashCode) and not equal (kit3.hashCode))
  }

  test("Addition of Part Kit to Database and find by id") {
    val kit1 = PartKit("Kit", Seq(line()))

    val partKit1 = PartKit.add(kit1)

    partKit1 should be('defined)

    PartKit.findById(partKit1.get.id.get) should be(partKit1)
  }

  test("Find Matching record by object id") {
    val kit1 = PartKit("Kit1", Seq( /*LineItem.create(0, part1, 1, 100, 0))*/ line()))
    val kitId = kit1.id.get
    val kit2 = PartKit("Kit2", Seq( /*LineItem.create(0, part1, 2, 100, 0)*/ line(quantity = 2))).id(kitId)
    val kit3 = PartKit("Kit3", Seq( /*LineItem.create(0, part1, 1, 200, 0)*/ line(price = 200d))).id(kitId)

    val addedKit = PartKit.add(kit1)
    addedKit should be(Some(kit1))
    addedKit.get.id.get should be(kitId)

    PartKit.findMatching(kit2) should be(Some(kit1))
    PartKit.findMatching(kit3) should be(Some(kit1))
  }

  test("Find Matching record by part kit name") {
    val kit1 = PartKit("Kit", Seq( /*LineItem.create(0, part1, 1, 100, 0)*/ line()))
    val kit2 = PartKit("Kit", Seq( /*LineItem.create(0, part1, 2, 100, 0)*/ line(quantity = 2)))
    val kit3 = PartKit("Kit", Seq( /*LineItem.create(0, part1, 1, 200, 0)*/ line(price = 200d)))

    val addedKit = PartKit.add(kit1)
    addedKit should be(Some(kit1))

    PartKit.findMatching(kit2) should be(Some(kit1))
    PartKit.findMatching(kit3) should be(Some(kit1))
  }

  test("Add record with matching object id returns the original record with the id and does not add extra records to the database") {
    val kit1 = PartKit("Kit1", Seq( /*LineItem.create(0, part1, 1, 100, 0)*/ line()))
    val kitId = kit1.id.get
    val kit2 = PartKit("Kit2", Seq( /*LineItem.create(0, part1, 2, 100, 0)*/ line(quantity = 2))).id(kitId)
    val kit3 = PartKit("Kit3", Seq( /*LineItem.create(0, part1, 1, 200, 0)*/ line(price = 200d))).id(kitId)

    val addedKit = PartKit.add(kit1)
    addedKit should be(Some(kit1))
    addedKit.get.id.get should be(kitId)

    PartKit.add(kit2) should be(Some(kit1))
    PartKit.fetch() should be(List(kit1))
    PartKit.add(kit3) should be(Some(kit1))
    PartKit.fetch() should be(List(kit1))
  }

  test("Add record with matching part kit name returns the original record with the id and does not add extra records to the database") {
    val kit1 = PartKit("Kit", Seq(line()))
    val kit2 = PartKit("Kit", Seq(line(quantity = 2)))
    val kit3 = PartKit("Kit", Seq(line(price = 200d)))

    val addedKit = PartKit.add(kit1)
    addedKit should be(Some(kit1))

    PartKit.findMatching(kit2) should be(Some(kit1))
    PartKit.fetch() should be(List(kit1))
    PartKit.findMatching(kit3) should be(Some(kit1))
    PartKit.fetch() should be(List(kit1))
  }

  test("Update record that exists") {
    val kit1 = PartKit("Kit1", Seq(line()))
    val oid = kit1.id.get
    PartKit.add(kit1)
    PartKit.findById(oid) should be(Some(kit1))

    val kit2 = PartKit("Kit2", Seq(line(quantity = 2)))
    PartKit.update(oid, kit2)
    PartKit.findById(oid) should be(Some(kit2))

    val kit3 = PartKit("Kit3", Seq(line(price = 200d)))
    PartKit.update(oid, kit3)
    PartKit.findById(oid) should be(Some(kit3))
  }

  test("Update record that does not exist returns None and does not add records to the database") {
    val kit1 = PartKit("Kit", Seq(line()))
    val oid = kit1.id.get
    PartKit.update(oid, kit1) should be('empty)

    PartKit.fetch() should be(Nil)
    PartKit.get() should be(None)
  }

  test("Remove existing record from database") {
    pending
  }

  test("Remove non existent record from database") {
    pending
  }
}