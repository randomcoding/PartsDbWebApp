/**
 *
 */
package uk.co.randomcoding.partsdb.core.customer

import uk.co.randomcoding.partsdb.core.contact.ContactDetails
import uk.co.randomcoding.partsdb.core.id.{Identifier, Identifiable, DefaultIdentifier}
import uk.co.randomcoding.partsdb.core.terms.PaymentTerms

/**
 * Customer information, including billing and delivery addresses and payment terms
 *
 * @constructor
 * @param customerId The unique id of this customer
 * @param customerName The short (friendly) name of the customer
 * @param billingAddress The [[uk.co.randomcoding.partsdb.core.address.AddressId]] of the address to send billing notices to
 * @param deliveryAddresses The [[uk.co.randomcoding.partsdb.core.address.AddressId]]s of the addresses to which goods can be delivered for this customer
 * @param terms The usual [[uk.co.randomcoding.partsdb.core.terms.PaymentTerms]] for this customer
 * @param contactDetails The contact name(s) and number(s) for this customer
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
case class Customer(val customerId: Identifier, val customerName: String, val billingAddress: Identifier, val deliveryAddresses: Set[Identifier], val terms: PaymentTerms, val contactDetails: ContactDetails) extends Identifiable {
  override val identifierFieldName = "customerId"
}

object DefaultCustomer extends Customer(DefaultIdentifier, "No Customer", DefaultIdentifier, Set.empty, PaymentTerms(-1), ContactDetails("No Details"))