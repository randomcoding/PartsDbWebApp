/**
 *
 */
package uk.co.randomcoding.partsdb.core.address

/**
 * @constructor
 * @param shortName The short (friendly) name of this Address
 * @param addressText The plain text version of the address
 * @param country The country this address is in
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
case class Address(val shortName: String, addressText: String, country: String)