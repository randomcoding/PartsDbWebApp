/**
 *
 */
package uk.co.randomcoding.partsdb.core.customer

import uk.co.randomcoding.partsdb.core.contact.ContactDetails
import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.id.{ Identifier, Identifiable, DefaultIdentifier }
import uk.co.randomcoding.partsdb.core.terms.PaymentTerms
import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.mongodb.record.field.MongoCaseClassField
import net.liftweb.mongodb.record.field.ObjectIdRefField
import net.liftweb.record.field.StringField

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
class Customer extends MongoRecord[Customer] with ObjectIdPk[Customer] {
  def meta = Customer

  object customerName extends StringField(this, 50)
  object businessAddress extends ObjectIdRefField(this, Address)
  object terms extends MongoCaseClassField[Customer, PaymentTerms](this)
  object contactDetails extends MongoCaseClassField[Customer, ContactDetails](this)
}

object Customer extends Customer with MongoMetaRecord[Customer]
