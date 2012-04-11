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
import uk.co.randomcoding.partsdb.core.document.{Document, DocumentType, LineItem}


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

  val quote1 = Document.create(Seq(LineItem.create(0, part1, 1, part1Cost, markup)), DocumentType.Quote, carriage)
  val quote2 = Document.create(Seq(LineItem.create(0, part2, 1, part2Cost, markup)), DocumentType.Quote, carriage)

  private[this] def saveDocuments(docTypes: DocumentType.DocType*) {
    Seq(quote1, quote2) filter (doc => docTypes.contains(doc.documentType.get)) foreach (doc => if (doc.save != doc) fail("Failed to save document %s".format(doc)))
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
    fail("Needs to be Implemented")
  }

  test("Value of Documents is correct for a transaction with quotes, orders and delivery notes") {
    fail("Needs to be Implemented")
  }

  test("Value of Documents is correct for a transaction with quotes, orders delivery notes and invoices") {
    fail("Needs to be Implemented")
  }
}
