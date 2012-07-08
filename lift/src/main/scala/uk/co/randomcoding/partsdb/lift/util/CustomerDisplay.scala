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
package uk.co.randomcoding.partsdb.lift.util

import scala.xml.{ Text, NodeSeq }

import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.contact.ContactDetails
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.lift.util._

import SnippetDisplayHelpers.{ displayContactCell, displayAddressCell }
import net.liftweb.common.Logger

/**
 * Helper functions for displaying customers in lift pages
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object CustomerDisplay extends TabularEntityDisplay with Logger {
  type EntityType = Customer
  /**
   * The headings to use for the display of the customer data table
   */
  override val rowHeadings = List("Customer Name", "Address", "Contact", "Payment Terms")

  /**
   * Generates html to display a customer.
   *
   * Currently displays the name, terms and contact details
   *
   * @param customer The [[uk.co.randomcoding.partsdb.core.customer.Customer]] to display
   * @return A [[scala.xml.NodeSeq]] of `<td>` elements to display the customer details
   */
  override def displayEntity(customer: Customer, editLink: Boolean, displayLink: Boolean): NodeSeq = {
    <td>{ customer.customerName }</td>
    <td>{ displayAddress(customer) }</td>
    <td>{ displayContacts(customer) }</td>
    <td>{ "%d days".format(customer.terms.get) }</td> ++
      editAndDisplayCells("Customer", customer.id.get, editLink, displayLink)
  }

  private[this] def displayAddress(customer: Customer) = {
    debug("Displaying Details for Customer: %s".format(customer))
    Address findById customer.businessAddress.get match {
      case Some(addr) => displayAddressCell(addr)
      case _ => Text("Unknown Address. Identifier: %s".format(customer.businessAddress.get))
    }
  }

  private[this] def displayContacts(customer: Customer): NodeSeq = customer.contactDetails.get flatMap (displayContactCell)
}
