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

import uk.co.randomcoding.partsdb.core.part.Part
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle
import uk.co.randomcoding.partsdb.core.transaction.Transaction
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.contact.ContactDetails
import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.document._
import uk.co.randomcoding.partsdb.core.supplier.Supplier

/**
 * Tests for the document value functions of a transaction
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class TransactionValueTest extends MongoDbTestBase {
  override val dbName = "TransactionValueTest"

  private[this] val address = Address.create("Addr2", "Addr2", "UK")
  private[this] val contacts = ContactDetails.create("Alan", "", "", "", "", true)
  private[this] val customer = Customer.create("Customer 2", address, 45, contacts)

  private[this] val part1 = Part.create("part 1", Vehicle.create("vehicle 1"), Some("ModId 1"))
  private[this] val part2 = Part.create("part 2", Vehicle.create("vehicle 2"), Some("ModId 2"))

  private[this] val part1Cost = 100.0d
  private[this] val part2Cost = 50.0d

  private[this] val markup = 0.1d
  private[this] val carriage = 10.0d

  private[this] val supplier = Supplier("Supplier", ContactDetails("Dave", "", "", "", "", true), Address("Addr1", "Address 1", "UK"), Nil)

  private[this] val lineItems1 = Seq(LineItem.create(0, part1, 1, part1Cost, markup, supplier))
  private[this] val lineItems2 = Seq(LineItem.create(0, part2, 1, part2Cost, markup, supplier))

  private[this] val quote1 = Quote.create(lineItems1, carriage)
  private[this] val quote2 = Quote.create(lineItems2, carriage)

  private[this] val order1 = Order.create(lineItems1, carriage, "order1")
  private[this] val order2 = Order.create(lineItems2, carriage, "order2")

  private[this] val deliveryNote1 = DeliveryNote.create(lineItems1, carriage, "order1")
  private[this] val deliveryNote2 = DeliveryNote.create(lineItems2, carriage, "order1")

  private[this] val invoice = Invoice.create(lineItems1 ++ lineItems2, carriage + carriage, "order1, order2", Seq(deliveryNote1, deliveryNote2))

  private[this] def saveDocuments(docTypes: DocumentType.DocType*) {
    Seq(quote1, quote2, order1, order2, deliveryNote1, deliveryNote2, invoice) filter
      (doc => docTypes.contains(doc.documentType.get)) foreach (doc => if (doc.save != doc) fail("Failed to save document %s".format(doc)))
  }

  test("Value of Documents is correct for a transaction with no documents") {
    val transaction = Transaction.create(customer, Nil)
    transaction.valueOfDocuments(DocumentType.Quote) should be(0.0d)
    transaction.valueOfDocuments(DocumentType.Order) should be(0.0d)
    transaction.valueOfDocuments(DocumentType.DeliveryNote) should be(0.0d)
    transaction.valueOfDocuments(DocumentType.Invoice) should be(0.0d)
  }

  test("Value of Documents is correct for a transaction with quotes only") {
    saveDocuments(DocumentType.Quote)

    val transaction = Transaction.add(customer, Seq(quote1, quote2)).get
    transaction.valueOfDocuments(DocumentType.Quote) should be(quote1.documentValue + quote2.documentValue)
    transaction.valueOfDocuments(DocumentType.Order) should be(0.0d)
    transaction.valueOfDocuments(DocumentType.DeliveryNote) should be(0.0d)
    transaction.valueOfDocuments(DocumentType.Invoice) should be(0.0d)
  }

  test("Value of Documents is correct for a transaction with quotes and orders") {
    saveDocuments(DocumentType.Quote, DocumentType.Order)

    val transaction = Transaction.add(customer, Seq(quote1, quote2, order1, order2)).get
    transaction.valueOfDocuments(DocumentType.Quote) should be(quote1.documentValue + quote2.documentValue)
    transaction.valueOfDocuments(DocumentType.Order) should be(order1.documentValue + order2.documentValue)
    transaction.valueOfDocuments(DocumentType.DeliveryNote) should be(0.0d)
    transaction.valueOfDocuments(DocumentType.Invoice) should be(0.0d)
  }

  test("Value of Documents is correct for a transaction with quotes, orders and delivery notes") {
    saveDocuments(DocumentType.Quote, DocumentType.Order, DocumentType.DeliveryNote)

    val transaction = Transaction.add(customer, Seq(quote1, quote2, order1, order2, deliveryNote1, deliveryNote2)).get
    transaction.valueOfDocuments(DocumentType.Quote) should be(quote1.documentValue + quote2.documentValue)
    transaction.valueOfDocuments(DocumentType.Order) should be(order1.documentValue + order2.documentValue)
    transaction.valueOfDocuments(DocumentType.DeliveryNote) should be(deliveryNote1.documentValue + deliveryNote2.documentValue)
    transaction.valueOfDocuments(DocumentType.Invoice) should be(0.0d)
  }

  test("Value of Documents is correct for a transaction with quotes, orders delivery notes and invoices") {
    saveDocuments(DocumentType.Quote, DocumentType.Order, DocumentType.DeliveryNote, DocumentType.Invoice)

    val transaction = Transaction.add(customer, Seq(quote1, quote2, order1, order2, deliveryNote1, deliveryNote2, invoice)).get
    transaction.valueOfDocuments(DocumentType.Quote) should be(quote1.documentValue + quote2.documentValue)
    transaction.valueOfDocuments(DocumentType.Order) should be(order1.documentValue + order2.documentValue)
    transaction.valueOfDocuments(DocumentType.DeliveryNote) should be(deliveryNote1.documentValue + deliveryNote2.documentValue)
    transaction.valueOfDocuments(DocumentType.Invoice) should be(invoice.documentValue)
  }
}
