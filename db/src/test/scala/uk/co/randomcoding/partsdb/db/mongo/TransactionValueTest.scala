/*
 * Copyright (c) 2012 RandomCoder <randomcoder@randomcoding.co.uk>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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


/**
 * Tests for the document value functions of a transaction
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class TransactionValueTest extends MongoDbTestBase {
  override val dbName = "TransactionValueTest"

  val address = Address.create("Addr2", "Addr2", "UK")
  val contacts = ContactDetails.create("Alan", "", "", "", "", true)
  val customer = Customer.create("Customer 2", address, 45, contacts)

  val part1 = Part.create("part 1", Vehicle.create("vehicle 1"), Some("ModId 1"))
  val part2 = Part.create("part 2", Vehicle.create("vehicle 2"), Some("ModId 2"))

  val part1Cost = 100.0d
  val part2Cost = 50.0d

  val markup = 0.1d
  val carriage = 10.0d

  val lineItems1 = Seq(LineItem.create(0, part1, 1, part1Cost, markup))
  val lineItems2 = Seq(LineItem.create(0, part2, 1, part2Cost, markup))

  val quote1 = Quote.create(lineItems1, carriage)
  val quote2 = Quote.create(lineItems2, carriage)

  val order1 = Order.create(lineItems1, carriage, "order1")
  val order2 = Order.create(lineItems2, carriage, "order2")

  val deliveryNote1 = DeliveryNote.create(lineItems1, carriage, "order1")
  val deliveryNote2 = DeliveryNote.create(lineItems2, carriage, "order1")

  val invoice = Invoice.create(lineItems1 ++ lineItems2, carriage + carriage, "order1, order2", Seq(deliveryNote1, deliveryNote2))

  private[this] def saveDocuments(docTypes: DocumentType.DocType*) {
    Seq(quote1, quote2, order1, order2, deliveryNote1, deliveryNote2, invoice) filter
      (doc => docTypes.contains(doc.documentType.get)) foreach (doc => if (doc.save != doc) fail("Failed to save document %s".format(doc)))
  }

  test("Value of Documents is correct for a transaction with no documents") {
    val transaction = Transaction.create("Trans", customer, Nil)
    transaction.valueOfDocuments(DocumentType.Quote) should be(0.0d)
    transaction.valueOfDocuments(DocumentType.Order) should be(0.0d)
    transaction.valueOfDocuments(DocumentType.DeliveryNote) should be(0.0d)
    transaction.valueOfDocuments(DocumentType.Invoice) should be(0.0d)
  }

  test("Value of Documents is correct for a transaction with quotes only") {
    saveDocuments(DocumentType.Quote)

    val transaction = Transaction.add("Trans 1", customer, Seq(quote1, quote2)).get
    transaction.valueOfDocuments(DocumentType.Quote) should be(quote1.documentValue + quote2.documentValue)
    transaction.valueOfDocuments(DocumentType.Order) should be(0.0d)
    transaction.valueOfDocuments(DocumentType.DeliveryNote) should be(0.0d)
    transaction.valueOfDocuments(DocumentType.Invoice) should be(0.0d)
  }

  test("Value of Documents is correct for a transaction with quotes and orders") {
    saveDocuments(DocumentType.Quote, DocumentType.Order)

    val transaction = Transaction.add("Trans 1", customer, Seq(quote1, quote2, order1, order2)).get
    transaction.valueOfDocuments(DocumentType.Quote) should be(quote1.documentValue + quote2.documentValue)
    transaction.valueOfDocuments(DocumentType.Order) should be(order1.documentValue + order2.documentValue)
    transaction.valueOfDocuments(DocumentType.DeliveryNote) should be(0.0d)
    transaction.valueOfDocuments(DocumentType.Invoice) should be(0.0d)
  }

  test("Value of Documents is correct for a transaction with quotes, orders and delivery notes") {
    saveDocuments(DocumentType.Quote, DocumentType.Order, DocumentType.DeliveryNote)

    val transaction = Transaction.add("Trans 1", customer, Seq(quote1, quote2, order1, order2, deliveryNote1, deliveryNote2)).get
    transaction.valueOfDocuments(DocumentType.Quote) should be(quote1.documentValue + quote2.documentValue)
    transaction.valueOfDocuments(DocumentType.Order) should be(order1.documentValue + order2.documentValue)
    transaction.valueOfDocuments(DocumentType.DeliveryNote) should be(deliveryNote1.documentValue + deliveryNote2.documentValue)
    transaction.valueOfDocuments(DocumentType.Invoice) should be(0.0d)
  }

  test("Value of Documents is correct for a transaction with quotes, orders delivery notes and invoices") {
    saveDocuments(DocumentType.Quote, DocumentType.Order, DocumentType.DeliveryNote, DocumentType.Invoice)

    val transaction = Transaction.add("Trans 1", customer, Seq(quote1, quote2, order1, order2, deliveryNote1, deliveryNote2, invoice)).get
    transaction.valueOfDocuments(DocumentType.Quote) should be(quote1.documentValue + quote2.documentValue)
    transaction.valueOfDocuments(DocumentType.Order) should be(order1.documentValue + order2.documentValue)
    transaction.valueOfDocuments(DocumentType.DeliveryNote) should be(deliveryNote1.documentValue + deliveryNote2.documentValue)
    transaction.valueOfDocuments(DocumentType.Invoice) should be(invoice.documentValue)
  }
}
