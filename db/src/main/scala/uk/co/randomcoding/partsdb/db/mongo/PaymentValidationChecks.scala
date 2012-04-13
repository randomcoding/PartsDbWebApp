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

import uk.co.randomcoding.partsdb.core.transaction.{InvoicePayment, Payment}

/**
 * Contains functions to perform validation checks on Payment operations
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object PaymentValidationChecks {

  /**
   * Checks that the `payment` has sufficient remaining unallocated value to satisfy all the `invoicePayments`
   *
   * If the payment is fully allocated then this will return a message to say so.
   * If the payment has insufficient remaining to pay al the `invoicePayments` then this will return a message to that effect instead.
   * Otherwise this will return an empty `Seq` indicating no errors
   */
  val paymentHasSufficientBalanceToPayTheInvoicePayments: (Payment, Seq[InvoicePayment]) => Seq[String] = (payment, invoicePayments) => {
    val amountToAllocate = invoicePayments.foldLeft(0d)(_ + _.paymentAmount.get)
    payment.isFullyAllocated match {
      case true => Seq("Payment %s has already been fully allocated. It is not possible to pay any more invoices with it".format(payment.paymentReference.get))
      case false => if (payment.unallocatedBalance >= amountToAllocate) Nil else Seq("The payment does not have sufficient available balance to pay £%.2f".format(amountToAllocate))
    }
  }
}
