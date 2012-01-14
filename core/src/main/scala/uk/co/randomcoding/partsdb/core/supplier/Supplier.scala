/**
 *
 */
package uk.co.randomcoding.partsdb.core.supplier

import uk.co.randomcoding.partsdb.core.contact.ContactDetails
import uk.co.randomcoding.partsdb.core.id.{ Identifier, Identifiable, DefaultIdentifier }

/**
 * Supplier information including contact details and a free form notes
 *
 * @constructor
 * @param supplierId The unique id of this supplier
 * @param supplierName The short (friendly) name of the supplierId
 * @param contactDetails The contact name(s) and number(s) for this supplier
 * @param notes Notes for this supplier
 *
 * @author Jane Rowe
 *
 */
case class Supplier(val supplierId: Identifier, val supplierName: String, val contactDetails: ContactDetails, val notes: String) extends Identifiable {
  override val identifierFieldName = "supplierId"
}

object DefaultSupplier extends Supplier(DefaultIdentifier, "No Supplier", ContactDetails("No Details"), "No Notes")