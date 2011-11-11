/**
 *
 */
package uk.co.randomcoding.partsdb.core.address

import uk.co.randomcoding.partsdb.core.id.{ Identifier, Identifiable }

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
case class Address(val addressId: Identifier, val shortName: String, val addressText: String, val country: String) extends Identifiable {
  override val identifierFieldName = "addressId"

  override def id = addressId.id
}