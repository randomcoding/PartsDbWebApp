/**
 *
 */
package uk.co.randomcoding.partsdb.core.address

import net.liftweb.mongodb.record.field._
import net.liftweb.mongodb.record.{ MongoRecord, MongoMetaRecord }
import net.liftweb.record.field.StringField

/**
 * @constructor Create a new address object
 * @param id The [[uk.co.randomcoding.partsdb.core.address.AddressId]] of this address. This is used for internal referencing of address objects from other entities.
 * @param shortName The short (friendly) name of this Address
 * @param addressText The plain text version of the address
 * @param country The country this address is in
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
class Address private () extends MongoRecord[Address] with ObjectIdPk[Address] {
  def meta = Address

  object shortName extends StringField(this, 50)
  object addressText extends StringField(this, 300)
  object country extends StringField(this, 50)
}

object Address extends Address with MongoMetaRecord[Address]
/*case class Address(val addressId: Identifier, val shortName: String, val addressText: String, val country: String) extends Identifiable {
  override val identifierFieldName = "addressId"
}

object NullAddress extends Address(Identifier(-1), "", "", "")*/ 