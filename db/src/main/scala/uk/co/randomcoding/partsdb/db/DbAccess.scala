/**
 *
 */
package uk.co.randomcoding.partsdb.db

import uk.co.randomcoding.partsdb.core.address.{ Address, AddressId }

/**
 * Defines the database access (getter) interface api for the system.
 *
 * By extending this trait and defining the
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
trait DbAccess {

  /**
   * Add an entity to the database
   */
  def add[T <: AnyRef](t: T): Unit

  /**
   * Get all addresses from the database
   */
  def addresses: Set[Address]

  /**
   * Get the address with the given id
   */
  def address(id: AddressId): Option[Address]

  /**
   * Convenience conversion for a long to an address id (which is just a typed wrapper for a Long anyway at the moment).
   */
  implicit def longToAddressId(id: Long): AddressId = AddressId(id)
}