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
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle
import util.Random
import uk.co.randomcoding.partsdb.core.part.Part
import uk.co.randomcoding.partsdb.core.document.{ Invoice, Document, LineItem }
import uk.co.randomcoding.partsdb.core.supplier.Supplier
import uk.co.randomcoding.partsdb.core.contact.ContactDetails
import uk.co.randomcoding.partsdb.core.address.Address

/**
 * TODO: Class Documentation
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class DocumentTest extends MongoDbTestBase with GivenWhenThen {
  override val dbName = "DocumentTests"

  private[this] lazy val lineFor100Pounds = lineItem("part1", 1, 100.0d)
  private[this] lazy val lineFor50Pounds = lineItem("part2", 1, 50.0d)
  private[this] lazy val invoiceFor100Pounds = invoice(lineFor100Pounds, "PoRef", 101)
  private[this] lazy val invoiceFor150Pounds = invoice(Seq(lineFor100Pounds, lineFor50Pounds), "PoRef2", 202)

  test("Remaining balance for an invoice that has no payments and has not been closed") {
    given("An invoice that has had not payments against it in the database")
    val inv = invoiceFor100Pounds
    when("The remaining balance is queried")
    val remaining = inv.remainingBalance
    then("The result is the same as the invoice's document value")
    remaining should be(inv.documentValue)
  }

  test("Remaining balance for non invoice documents is always the same as the document value") {
    pending
  }

  private[this] implicit def itemToList[T](item: T): List[T] = List(item)

  private[this] val vehicle = Vehicle.create("Vehicle")

  private[this] val supplier = Supplier("Supplier", ContactDetails("Dave", "", "", "", "", true), Address("Addr1", "Address 1", "UK"), Nil)

  private[this] def lineItem(partName: String, quantity: Int, price: Double): LineItem = LineItem.create(Random.nextInt(1000), Part.create(partName, vehicle), quantity, price, 0d, supplier)

  private[this] def invoice(lines: Seq[LineItem], poRef: String, documentNumber: Int): Document = Invoice(lines, 0d, poRef).docNumber(documentNumber)
}
