/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util

import uk.co.randomcoding.partsdb.core.supplier.Supplier
import SnippetDisplayHelpers._
import scala.xml.NodeSeq
import uk.co.randomcoding.partsdb.core.address.Address
import scala.xml.Text
import uk.co.randomcoding.partsdb.core.contact.ContactDetails

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object SupplierDisplay extends EntityDisplay {

  override type EntityType = Supplier

  override val rowHeadings = List("Supplier Name", "Business Address", "Contact Details")

  override def displayEntity(supplier: Supplier): NodeSeq = {
    <td>{ supplier.supplierName.get }</td>
    <td>{ displayAddress(supplier) }</td>
    <td>{ displayContacts(supplier) }</td> ++
      editEntityCell(editEntityLink("Supplier", supplier.id.get))
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