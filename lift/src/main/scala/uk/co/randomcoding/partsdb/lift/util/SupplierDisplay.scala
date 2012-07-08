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
import uk.co.randomcoding.partsdb.core.supplier.Supplier
import uk.co.randomcoding.partsdb.lift.util._

import SnippetDisplayHelpers.{ displayContactCell, displayAddressCell }

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object SupplierDisplay extends TabularEntityDisplay {

  override type EntityType = Supplier

  override val rowHeadings = List("Supplier Name", "Business Address", "Contact Details")

  override def displayEntity(supplier: Supplier, editLink: Boolean, displayLink: Boolean): NodeSeq = {
    <td>{ supplier.supplierName.get }</td>
    <td>{ displayAddress(supplier) }</td>
    <td>{ displayContacts(supplier) }</td> ++
      editAndDisplayCells("Supplier", supplier.id.get, editLink, displayLink)
  }

  private[this] def displayAddress(supplier: Supplier): NodeSeq = Address findById supplier.businessAddress.get match {
    case Some(a) => displayAddressCell(a)
    case _ => Text("Unknown Address. Identifier: %s".format(supplier.businessAddress.get))
  }

  private[this] def displayContacts(supplier: Supplier): NodeSeq = supplier.contactDetails.get match {
    case c: ContactDetails => displayContactCell(c)
    case _ => Text("Unknown Contact. Identifier: %s".format(supplier.contactDetails.get))
  }

}
