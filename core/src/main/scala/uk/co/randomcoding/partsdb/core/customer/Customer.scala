/**
 *
 */
package uk.co.randomcoding.partsdb.core.customer

import uk.co.randomcoding.partsdb.core._
import address.Address
import terms.PaymentTerms
import uk.co.randomcoding.partsdb.core.contact.ContactDetails

/**
 * Customer information, including billing and delivery addresses and payment terms
 *
 * @constructor
 * @param customerName The short (friendly) name of the customer
 * @param billingAddress The [[uk.co.randomcoding.partsdb.core.address.Address]] to send billing notices to
 * @param deliveryAddresses The [[uk.co.randomcoding.partsdb.core.address.Address]]es to which goods can be delivered for this customer
 * @param terms The usual [[uk.co.randomcoding.partsdb.core.terms.PaymentTerms]] for this customer
 * @param contactDetails The contact name(s) and number(s) for this customer
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
case class Customer(customerName: String, billingAddress: Address, deliveryAddresses: Set[Address], terms: PaymentTerms, contactDetails: ContactDetails)