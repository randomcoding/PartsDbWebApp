/**
 *
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

  private[this] def displayContacts(supplier: Supplier): NodeSeq = ContactDetails findById supplier.contactDetails.get match {
    case Some(c) => displayContactCell(c)
    case _ => Text("Unknown Contact. Identifier: %s".format(supplier.contactDetails.get))
  }

}