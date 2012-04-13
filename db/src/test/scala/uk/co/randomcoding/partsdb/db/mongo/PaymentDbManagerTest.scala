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

import org.scalatest.GivenWhenThen
import uk.co.randomcoding.partsdb.core.document.{Invoice, LineItem, Document}
import util.Random
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle
import uk.co.randomcoding.partsdb.core.part.Part
import com.foursquare.rogue.Rogue._
import uk.co.randomcoding.partsdb.core.transaction.{Transaction, Payment, InvoicePayment}

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class PaymentDbManagerTest extends MongoDbTestBase with GivenWhenThen {
  override val dbName = "paymentdbmanagertest"

  private[this] lazy val lineFor100Pounds = lineItem("part1", 1, 100.0d)
  private[this] lazy val lineFor50Pounds = lineItem("part2", 1, 50.0d)
  private[this] lazy val invoiceFor100Pounds = invoice(lineFor100Pounds, "PoRef")
  private[this] lazy val invoiceFor150Pounds = invoice(Seq(lineFor100Pounds, lineFor50Pounds), "PoRef2")

  /*
  * Payments that have errors
  */
  test("Attempt to commit a new payment that does not have sufficient value to pay all the invoices") {
    given("An Invoice Payment for £100")
    val invoicePayment = InvoicePayment(invoiceFor100Pounds, 100.0d)
    and("A Payment object for £50 with no invoices already paid by it")
    val payment = Payment(50.0, "pay1", Nil)
    when("The payment is committed to the database")
    val response = PaymentDbManager.commitPayment(payment, invoicePayment)
    then("An error is raised")
    response should be(List(PaymentFailed("The payment does not have sufficient available balance to pay £100.00")))
    and("The database is not updated")
    databaseChecks()
  }

  test("Attempt to commit a payment that has already been partially allocated to some invoices that does not have sufficient value to pay all the additional invoices") {
    given("An Invoice Payment for £100")
    val invoicePayment = InvoicePayment(invoiceFor100Pounds, 100.0d)
    and("A Payment for £200 that has already had £150 allocated to")
    val allocatedInvoicePayment = InvoicePayment(invoiceFor150Pounds, 150.0d)
    Document.add(invoiceFor150Pounds)
    val payment = Payment(200.0, "pay2", allocatedInvoicePayment)
    when("The payment is committed to the database")
    val response = PaymentDbManager.commitPayment(payment, invoicePayment)
    then("An error is raised")
    response should be(List(PaymentFailed("The payment does not have sufficient available balance to pay £100.00")))
    and("The database is not updated")
    databaseChecks(expectedDocuments = invoiceFor150Pounds)
  }

  test("Attempt to commit an Invoice Payment with a Payment that is already fully allocated") {
    given("A Payment that is fully allocated")
    val allocatedInvoicePayment = InvoicePayment(invoiceFor150Pounds, 150.0d)
    Document.add(invoiceFor150Pounds)
    val payment = Payment(150.0, "pay3", allocatedInvoicePayment)
    and("An Invoice Payment of any type")
    val invoicePayment = InvoicePayment(invoiceFor100Pounds, 100.0d)
    when("The Payment is committed")
    val response = PaymentDbManager.commitPayment(payment, invoicePayment)
    then("An error is raised for the Payment already being fully allocated")
    response should be(List(PaymentFailed("Payment pay3 has already been fully allocated. It is not possible to pay any more invoices with it")))
    and("The database is not updated")
    databaseChecks(expectedDocuments = invoiceFor150Pounds)
  }

  test("Attempt to commit an Invoice Payment for an Invoice that is not in the database") {
    fail("Needs to be implemented")
  }

  test("Attemp to pay an invoice that is already closed") {
    fail("Needs to be implemented")
  }

  test("Attempt to pay more that the total amount of an invoice's value") {
    fail("Needs to be implemented")
  }

  test("Attempt to pay more than an invoice's total value in two partial payments") {
    fail("Needs to be implemented")
  }

  /*
  * Payments for a single invoice and Transactions with a single invoice
  */
  test("Payment of a Transaction with a single document stream correctly closes the invoice and completes Transaction") {
    given("A Transaction with a single invoice that has not been paid")
    and("An Invoice Payment for the full amount of the invoice")
    and("A Payment Object that only contains that invoice")
    when("The payment is committed")
    then("The Invoice is matked as closed")
    and("The Transaction is completed")
    and("The Payment is marked as fully allocated")
    and("The Invoice Payment is paid in full")
    fail("Needs to be implemented")
  }

  test("A Partial Payment for a single invoice in a Transaction with a single invoice does not close the invoice and the transaction is not marked as closed") {
    given("A Transaction with a single unpaid invoice")
    and("Two Payment objects each for half the value of the invoice")
    and("Two Invoice Payments that each pay half of the invoice")
    when("The two payments are committed")
    then("The Invoice is marked as closed")
    and("The Transaction is completed")
    and("The Payment is marked as fully allocated")
    and("The Invoice Payment is paid in full")
    fail("Needs to be implemented")
  }

  test("Two Partial Payments for a single invoice that make up the full amount in a Transaction with a single invoice correctly closes the invoice and completed the transaction") {
    given("A Transaction with a single unpaid invoice")
    and("Two Payment objects each for half the value of the invoice")
    and("Two Invoice Payments that each pay half of the invoice")
    when("The two payments are committed")
    then("The Invoice is marked as closed")
    and("The Transaction is completed")
    and("The Payment is marked as fully allocated")
    and("The Invoice Payment is paid in full")
    fail("Needs to be implemented")
  }

  /*
  * Payments for a single invoice and Transactions with a multiple invoices
  */
  test("Payment for the full amount of one of multiple invoices in a Transaction correctly closes the invoice, but does not complete the Transaction") {
    given("A Transaction that contains two unpaid invoices")
    and("A Payment for the full amount of one invoice in the transaction")
    and("An Invoice Payment that pays the full value of the invoice")
    when("The payment is committed")
    then("The paid Invoice is marked as closed")
    and("The other Invoices in the transaction are still not closed")
    and("The Transaction is still in the Invoiced state")
    and("The Payment is marked as fully allocated")
    and("The Invoice Payment is paid in full")
    fail("Needs to be implemented")
  }

  test("Partial Payment for one of multiple invoices in a Transaction does not close the invoice and does not complete the Transaction") {
    given("A Transaction that contains two unpaid invoices")
    and("A Payment for less than the full amount of one invoice in the transaction")
    and("An Invoice Payment that pays the partial value of the invoice")
    when("The payment is committed")
    then("The paid Invoice is not marked as closed")
    and("The other Invoices in the transaction are still not closed")
    and("The Transaction is still in the Invoiced state")
    and("The Payment is marked as fully allocated")
    and("The Invoice Payment is paid in full")
    fail("Needs to be implemented")
  }

  test("Two Partial Payments for a single invoice that make up the full amount in a Transaction with multiple invoices correctly closes the invoice but does not completed the transaction") {
    given("A Transaction that contains two unpaid invoices")
    and("Two Payments for half the amount of one invoice in the transaction")
    and("Two Invoice Payments that each pay half the value of the invoice")
    when("The two payments are committed")
    then("The paid Invoice is marked as closed")
    and("The other Invoices in the transaction are still not closed")
    and("The Transaction is still in the Invoiced state")
    and("The Payments are marked as fully allocated")
    and("The Invoice Payments are not paid in full")
    fail("Needs to be implemented")
  }

  /*
  * Payments for a multiple invoices and Transactions with a multiple invoices
  */
  test("Full Payment for all invoices in a Transaction with a multiple document stream correctly closes all invoice and completes Transaction") {
    given("A Transaction with three invoices")
    and("A Single Payment for the full value of all three invoices")
    and("Invoice Payments for the full amount of each invoice")
    when("The payments are committed")
    then("All three invoices are marked as closed")
    and("The Transaction is in the Completed state")
    and("The Payment is marked as fully allocated")
    and("The Invoice Payments are all paid in full")
    fail("Needs to be implemented")
  }

  test("Partial Payment for two invoices in a Transaction (with two invoices) does not close the invoices and does not complete the Transaction") {
    given("A Transaction with two invoices")
    and("A Payment that is less than the value of both invoices")
    and("Two invoice Payments that allocate the entire payment and each pay part of one invoice")
    when("The Payments are committed")
    then("Each invoice is not marked as closed")
    and("The payment is marked as fully allocated")
    and("The Invoice Payments are not paid in full")
    fail("Needs to be implemented")
  }

  test("Two Partial Payments for two invoices (that make up the full amount) in a Transaction (with two invoices) close the invoices and completes the Transaction") {
    given("A Tranaction with two invoices")
    and("A Payment for part of the value of each invoice")
    and("Two Invoice Payments for part of the value of each invoice")
    and("Another Payment for the remaining value of the two invoices")
    and("Two Invoice Payments for the remaining balance of each invoice")
    when("The payments are all committed")
    then("The Invoices are marked as closed")
    and("The Transaction is in the Completed state")
    and("The two payments are marked as fully allocated")
    and("The four Invoice Payments are all not paid in full")
    fail("Needs to be implemented")
  }

  test("A payment for the full amount of one invoice and part of the value of a second in a Transaction with two invoices closes the fully paid invoice but does not close the other and does not complete the Transaction") {
    given("A Transaction with two invoices")
    and("A Payment for the value of one whole invoice and part of the other")
    and("One Invoice Payment for the full value of one invoice")
    and("One Invoice Payment for part of the value of the other invoice")
    when("The Payments are committed")
    then("The fully paid invoice is marked as closed")
    and("The partially paid invoice is not marked as closed")
    and("The Payment is marked as fully allocated")
    and("The InvoicePayment for the paid invoice is fully paid")
    and("The InvoicePayment for the partially paid invoice is not fully paid")
    and("The transaction is still in the Invoiced state")
    fail("Needs to be implemented")
  }

  /*
   * Payments that affect invoices from multiple transactions
   */
  test("Full payment for two invoices in different Transactions, where each transaction only has the single invoice correctly closes both invoices and completes both Transactions") {
    fail("Needs to be implemented")
  }

  test("Part Payment for two invoices in different transactions where each Transaction only has the single invoice does not close either invoice nor complete either transaction") {
    fail("Needs to be implemented")
  }

  test("Payment for two invoices in different transactions, on full and one partial, where each Transaction only has the single invoice closes the fully paid invoice and copmletes the relevant Transaction, but does not close the partially paid invoice nor complete its transaction") {
    fail("Needs to be implemented")
  }

  private[this] val vehicle = Vehicle.create("Vehicle")

  private[this] def lineItem(partName: String, quantity: Int, price: Double): LineItem = LineItem.create(Random.nextInt(1000), Part.create(partName, vehicle), quantity, price, 0d)

  private[this] def invoice(lines: Seq[LineItem], poRef: String): Document = Invoice(lines, 0d, poRef)

  private[this] implicit def itemToList[T](item: T): List[T] = List(item)

  private[this] def databaseChecks(expectedPayments: List[Payment] = Nil, expectedDocuments: List[Document] = Nil, expectedTransactions: List[Transaction] = Nil) {
    Payment where (_.id exists true) fetch() should be(expectedPayments)
    Document where (_.id exists true) fetch() should be(expectedDocuments)
    Transaction where (_.id exists true) fetch() should be(expectedTransactions)
  }
}

