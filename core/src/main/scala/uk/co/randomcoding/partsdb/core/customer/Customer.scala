/**
 *
 */
package uk.co.randomcoding.partsdb.core.customer

import uk.co.randomcoding.partsdb.core._
import address.Address
import terms.PaymentTerms
import contact.ContactDetails
import address.AddressId
import id._

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
case class Customer(val customerId: CustomerId, val customerName: String, val billingAddress: AddressId, val deliveryAddresses: Set[AddressId], val terms: PaymentTerms, val contactDetails: ContactDetails)