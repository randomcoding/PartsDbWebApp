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
import util.Random
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle
import uk.co.randomcoding.partsdb.core.part.Part
import com.foursquare.rogue.Rogue._
import uk.co.randomcoding.partsdb.core.transaction.{Transaction, Payment, InvoicePayment}
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.contact.ContactDetails
import java.util.Date
import uk.co.randomcoding.partsdb.core.document._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class PaymentDbManagerTest extends MongoDbTestBase with GivenWhenThen {

  import PaymentDbManager.commitPayment

  override val dbName = "paymentdbmanagertest"

  private[this] lazy val lineFor100Pounds = lineItem("part1", 1, 100.0d)
  private[this] lazy val lineFor50Pounds = lineItem("part2", 1, 50.0d)
  private[this] lazy val invoiceFor100Pounds = invoice(lineFor100Pounds, "PoRef", 101)
  private[this] lazy val invoiceFor150Pounds = invoice(Seq(lineFor100Pounds, lineFor50Pounds), "PoRef2", 202)
  private[this] lazy val orderFor100Pounds = Order(lineFor100Pounds, 0, "PoRef")
  private[this] lazy val deliveryFor100Pounds = DeliveryNote(lineFor100Pounds, 0, "PoRef")

  private[this] lazy val transactionFor100Pounds = Transaction.create("Trans 1", customer("Customer"), Seq(orderFor100Pounds, deliveryFor100Pounds, invoiceFor100Pounds))

  private[this] val successfulPaymentResponse = List(PaymentSuccessful)

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
    response should contain(PaymentFailed("The payment does not have sufficient available balance to pay £100.00").asInstanceOf[PaymentResult])
    and("The database is not updated")
    performDatabaseChecks()
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
    response should contain(PaymentFailed("The payment does not have sufficient available balance to pay £100.00").asInstanceOf[PaymentResult])
    and("The database is not updated")
    performDatabaseChecks(expectedDocuments = invoiceFor150Pounds)
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
    response should contain(PaymentFailed("Payment pay3 has already been fully allocated. It is not possible to pay any more invoices with it").asInstanceOf[PaymentResult])
    and("The database is not updated")
    performDatabaseChecks(expectedDocuments = invoiceFor150Pounds)
  }

  test("Attempt to commit an Invoice Payment for an Invoice that is not in the database") {
    given("A Payment for £100")
    val payment = Payment(100.0, "pay4", Nil)
    and("An Invoice Payment for an invoice that is not stored in the database")
    val invoicePayment = InvoicePayment(invoiceFor100Pounds, 100.0)
    when("The Payment is committed")
    val response = commitPayment(payment, invoicePayment)
    then("An error indicating the invoice was not found is returned")
    response should contain(PaymentFailed("Invoice for payment of £100.00 was not found in the database.\nObject Id: %s".format(invoiceFor100Pounds.id.get)).asInstanceOf[PaymentResult])
    and("The database is not updated")
    performDatabaseChecks()
  }

  test("Attempt to commit an Invoice Payment for two Invoices that are not in the database") {
    given("A Payment for £100")
    val payment = Payment(250.0, "pay5", Nil)
    and("Two Invoice Payments for invoices that are not stored in the database")
    val invoicePayment1 = InvoicePayment(invoiceFor100Pounds, 100.0)
    val invoicePayment2 = InvoicePayment(invoiceFor150Pounds, 150.0)
    when("The Payment is committed")
    val response = commitPayment(payment, invoicePayment1, invoicePayment2)
    then("An error indicating neither of the invoices were found is returned")
    response should (have size (2) and
        contain(PaymentFailed("Invoice for payment of £100.00 was not found in the database.\nObject Id: %s".format(invoiceFor100Pounds.id.get)).asInstanceOf[PaymentResult]) and
        contain(PaymentFailed("Invoice for payment of £150.00 was not found in the database.\nObject Id: %s".format(invoiceFor150Pounds.id.get)).asInstanceOf[PaymentResult]))
    and("The database is not updated")
    performDatabaseChecks()
  }

  test("Attempt to pay an invoice that is already closed") {
    given("A Payment for £100")
    val payment = Payment(100.0, "pay6", Nil)
    and("An invoice that is in the database and already marked as closed")
    Document.add(invoiceFor100Pounds)
    Document.close(invoiceFor100Pounds.id.get)
    and("An Invoice Payment for the closed invoice")
    val invoicePayment = InvoicePayment(invoiceFor100Pounds, 100.00)
    when("The Payment is committed")
    val response = commitPayment(payment, invoicePayment)
    then("An error reporting the invoice is already closed is returned")
    response should contain(PaymentFailed("Invoice INV000101 has already been closed").asInstanceOf[PaymentResult])
    and("The database is not updated")
    performDatabaseChecks(expectedDocuments = List(invoiceFor100Pounds))
  }

  test("Attempt to pay more that the remaining balance of an invoice (in this case the full amount)") {
    given("A Payment for £150")
    val payment = Payment(150.0, "pay7", Nil)
    and("An invoice that is in the database for £100")
    Document.add(invoiceFor100Pounds)
    and("An Invoice Payment for £150 against the invoice for £100")
    val invoicePayment = InvoicePayment(invoiceFor100Pounds, 150.00)
    when("The Payment is committed")
    val response = commitPayment(payment, invoicePayment)
    then("An error reporting the payment is for more than the remaining balance of the invoice is returned")
    response should be(List(PaymentFailed("Invoice INV000101 only has £100.00 outstanding. It is not possible to allocate £150.00 to this invoice")))
    and("The Database is not updated")
    performDatabaseChecks(expectedDocuments = List(invoiceFor100Pounds))
  }

  test("Attempt to pay more that the remaining balance of an invoice (in this case the full amount) in a payment for two invoices") {
    given("A Payment for £300")
    val payment = Payment(300.0, "pay7", Nil)
    and("An invoice that is in the database for £100")
    Document.add(invoiceFor100Pounds)
    and("An invoice that is in the database for £150")
    Document.add(invoiceFor150Pounds)
    and("An Invoice Payment for £150 against the invoice for £100")
    val invoicePayment1 = InvoicePayment(invoiceFor100Pounds, 150.00)
    and("An Invoice Payment for £150 against the invoice for £150")
    val invoicePayment2 = InvoicePayment(invoiceFor150Pounds, 150.00)
    when("The Payment is committed")
    val response = commitPayment(payment, invoicePayment1, invoicePayment2)
    then("An error reporting the payment is for more than the remaining balance of the invoice for £100 is returned")
    response should be(List(PaymentFailed("Invoice INV000101 only has £100.00 outstanding. It is not possible to allocate £150.00 to this invoice")))
    and("The Database is not updated")
    performDatabaseChecks(expectedDocuments = List(invoiceFor100Pounds, invoiceFor150Pounds))
    val inv1 = Document.findById(invoiceFor150Pounds.id.get).get
    inv1.editable.get should be(true)
    inv1.remainingBalance should be(150.00)
    val inv2 = Document.findById(invoiceFor100Pounds.id.get).get
    inv2.editable.get should be(true)
    inv2.remainingBalance should be(100.00)

  }

  test("Attempt to pay more than an invoice's remaining balance with the second of two partial payments") {
    given("An invoice for £100 in the database")
    and("A successful Payment of £50 against that invoice")
    and("Another Payment for £75")
    and("A corresponding Invoice Payment for £75 against the same invoice")
    when("The Second payment is committed")
    then("An error reporting the second payment is for more that the remaining balance on the invoice is returned")
    and("The database is not updated with any of the details from the second payment")
    fail("Needs to be implemented")
  }

  /*
  * Payments for a single invoice and Transactions with a single invoice
  */
  test("Payment of a Transaction with a single document stream correctly closes the invoice and completes Transaction") {
    given("A Transaction for £100 with order, delivery and invoice correctly closed")
    val transaction = setupTransactionFor100Pounds
    and("An Invoice Payment for the full amount of the invoice")
    val invoicePayment = InvoicePayment(getCurrentInvoiceFromDb(invoiceFor100Pounds), 100.00)
    and("A Payment Object that only contains that invoice")
    val payment = Payment(100, "pay101", Nil)
    when("The payment is committed")
    val response = commitPayment(payment, invoicePayment)
    then("The response is Payment Success")
    response should be(List(PaymentSuccessful))
    and("The Invoice is marked as closed")
    val updatedInv = Document.findById(invoiceFor100Pounds.id.get).get
    updatedInv.editable.get should be(false)
    and("The Transaction is completed")
    val updatedTransaction = Transaction.findById(transaction.id.get).get
    updatedTransaction.completionDate.get should not be (new Date(0))
    updatedTransaction.transactionState should be("Completed")
    and("The Payment is marked as fully allocated")
    val updatedPayment = Payment.findById(payment.id.get).get
    updatedPayment.isFullyAllocated should be(true)
    updatedPayment.unallocatedBalance should be(0)
    and("The Invoice Payment is paid in full")
    updatedPayment.paidInvoices.get map (_.paidInFull) should be(List(Some(true)))
  }

  test("A Partial Payment for a single invoice in a Transaction with a single invoice does not close the invoice and the transaction is not marked as closed") {
    given("A Transaction with a single unpaid invoice")
    val transaction = setupTransactionFor100Pounds
    and("A Payment objects each for half the value of the invoice")
    val payment = Payment(50.0, "pay102", Nil)
    and("An Invoice Payment that pays half of the invoice")
    val invPayment = invoicePayment(invoiceFor100Pounds, 50.0)
    when("The payment is committed")
    commitPayment(payment, invPayment) should be(successfulPaymentResponse)
    then("The Invoice is not marked as closed")
    val inv = getCurrentInvoiceFromDb(invoiceFor100Pounds)
    inv.editable.get should be(true)
    and("The invoice still has £50 remaining")
    inv.remainingBalance should be(50.0d)
    and("The Transaction is still in the Invoiced state")
    Transaction.findById(transaction.id.get).get.transactionState should be("Invoiced")
    and("The Payment is marked as fully allocated")
    val updatedPayment = Payment.findById(payment.id.get).get
    updatedPayment.isFullyAllocated should be(true)
    and("The Invoice Payment is not paid in full")
    updatedPayment.paidInvoices.get should have size (1)
    updatedPayment.paidInvoices.get(0).paidInFull should be(Some(false))
  }

  test("Two Partial Payments for a single invoice that make up the full amount in a Transaction with a single invoice correctly closes the invoice and completed the transaction") {
    given("A Transaction with a single unpaid invoice for £100")
    and("A partial payment for £50 already allocated to the invoice")
    and("Another partial payment for the reamining £50 of the invoice's balance")
    when("The second payment is committed")
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

  private[this] def invoice(lines: Seq[LineItem], poRef: String, documentNumber: Int): Document = Invoice(lines, 0d, poRef).docNumber(documentNumber)

  private[this] def customer(customerName: String): Customer = Customer.create(customerName, Address.create("Address", "Address", "United Kingdom"), 30, ContactDetails.create("Dave", "", "", "", "", true))

  private[this] def invoicePayment(invoice: Document, paymentValue: Double): InvoicePayment = InvoicePayment(getCurrentInvoiceFromDb(invoice), paymentValue)

  private[this] implicit def itemToList[T](item: T): List[T] = List(item)

  private[this] def performDatabaseChecks(expectedPayments: List[Payment] = Nil, expectedDocuments: List[Document] = Nil, expectedTransactions: List[Transaction] = Nil) {
    Payment where (_.id exists true) fetch() should be(expectedPayments)
    Document where (_.id exists true) fetch() should be(expectedDocuments)
    Transaction where (_.id exists true) fetch() should be(expectedTransactions)
  }

  private[this] def setupTransactionFor100Pounds: Transaction = {
    Document.add(invoiceFor100Pounds).get
    Document.add(orderFor100Pounds)
    Document.close(orderFor100Pounds.id.get) should be('defined)
    Document.add(deliveryFor100Pounds)
    Document.close(deliveryFor100Pounds.id.get) should be('defined)

    Transaction.add(transactionFor100Pounds).get
  }

  private[this] def getCurrentInvoiceFromDb(inv: Document): Document = Document.findById(inv.id.get).get
}

