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

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class PaymentDbManagerTest extends MongoDbTestBase with GivenWhenThen {
  override val dbName = "paymentdbmanagertest"


  // TODO: Write tests

  /*
  * Payments that have errors
  */
  test("Attempt to commit a new payment that does not have sufficient value to pay all the invoices") {
    given("An Invoice Payment for £100")
    and("A Payment object for £50")
    when("The payment is committed to the database")
    then("An error is raised")
    and("The database is not updated")
    fail("Needs to be implemented")
  }

  test("Attempt to commit a payment that has already been partially allocated to some invoices that does not have sufficient value to pay all the additional invoices") {
    given("An Invoice Payment for £100")
    and("A Payment for £200 that has already had £150 allocated to")
    when("The payment is committed to the database")
    then("An error is raised")
    and("The database is not updated")
    fail("Needs to be implemented")
  }

  /*
  * Payments for a single invoice and Transactions with a single invoice
  */
  test("Payment of a Transaction with a single document stream correctly closes the invoice and completes Transaction") {
    given("A Transaction with a single invoice that has not been paid")

    and("An Invoice Payment for the full amount of the invoice")
    fail("Needs to be implemented")
  }

  test("Two Partial Payments for a single invoice that make up the full amount in a Transaction with a single invoice correctly closes the invoice and completed the transaction") {
    fail("Needs to be implemented")
  }

  /*
  * Payments for a single invoice and Transactions with a multiple invoices
  */
  test("Payment for the full amount of one of multiple invoices in a Transaction correctly closes the invoice, but does not complete the Transaction") {
    fail("Needs to be implemented")
  }

  test("Partial Payment for one of multiple invoices in a Transaction does not close the invoice and does not complete the Transaction") {
    fail("Needs to be implemented")
  }

  test("Two Partial Payments for a single invoice that make up the full amount in a Transaction with multiple invoices correctly closes the invoice but does not completed the transaction") {
    fail("Needs to be implemented")
  }

  /*
  * Payments for a multiple invoices and Transactions with a multiple invoices
  */
  test("Full Payment for all invoices in a Transaction with a multiple document stream correctly closes all invoice and completes Transaction") {
    fail("Needs to be implemented")
  }

  test("Partial Payment for two invoices in a Transaction (with two invoices) does not close the invoices and does not complete the Transaction") {
    fail("Needs to be implemented")
  }

  test("Two Partial Payments for two invoices (that make up the full amount) in a Transaction (with two invoices) close the invoices and completes the Transaction") {
    fail("Needs to be implemented")
  }

  test("A payment for the full amount of one invoice and part of the value of a second in a Transaction with two invoices closes the fully paid invoice but does not close the other and does not complete the Transaction") {
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

}
