/**
 *
 */
package uk.co.randomcoding.partsdb.core.address

import net.liftweb.mongodb.record.field._
import net.liftweb.mongodb.record.{ MongoRecord, MongoMetaRecord }
import net.liftweb.record.field.StringField
import com.foursquare.rogue.Rogue._

/**
 * A simple representation of an address.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
class Address private () extends MongoRecord[Address] with ObjectIdPk[Address] {
  def meta = Address

  object shortName extends StringField(this, 50)
  object addressText extends StringField(this, 300)
  object country extends StringField(this, 50)

  override def equals(that: Any): Boolean = {
    that.isInstanceOf[Address] match {
      case false => false
      case true => {
        val other = that.asInstanceOf[Address]
        shortName.get == other.shortName.get && addressText.get == other.addressText.get && country.get == other.country.get
      }
    }
  }

  override def hashCode: Int = getClass.hashCode + shortName.get.hashCode + addressText.get.hashCode + country.get.hashCode
}

object Address extends Address with MongoMetaRecord[Address] {
  import org.bson.types.ObjectId

  /**
   * Get the address with the given id.
   *
   * @return An Option containing the address that is found, or `None` if there is no address with the given identifier
   */
  def findById(oid: ObjectId) = Address where (_.id eqs oid) get

  /**
   * Find all addresses that have the given short name.
   *
   * As we try to ensure that all short names are unique, this '''should''' only ever return at most a single result
   */
  def findNamed(shortName: String) = Address where (_.shortName eqs shortName) fetch

  /**
   * Finds all addresses with the given `addressText`.
   *
   * The likelihood is that this will return at most a single result.
   */
  def findByAddressText(addressText: String) = Address where (_.addressText eqs addressText) fetch

  /**
   * Remove the address with the given `shortName`
   */
  def remove(shortName: String) = findNamed(shortName) map (_.delete_!) distinct

  /**
   * Modify '''''all''''' the properties of the address with the given `ObjectId`
   *
   * If you don't want to change a value, then provide the original value
   *
   * @param newShortName The new short name to set
   * @param newAddressText The new address text to set
   * @param newCountry The new country to set
   */
  def modify(oid: ObjectId, newShortName: String, newAddressText: String, newCountry: String): Unit = {
    Address.where(_.id eqs oid).modify(_.shortName setTo newShortName) and (_.addressText setTo newAddressText) and (_.country setTo newCountry) updateMulti
  }

  /**
   * Modify an address by populating its fields with the values from the `newAddress`
   */
  def modify(oid: ObjectId, newAddress: Address): Unit = modify(oid, newAddress.shortName.get, newAddress.addressText.get, newAddress.country.get)

  /**
   * Add a new address constructed from the paramters unless a matching record is found.
   *
   * @return `Some(address)` if a match was made, or the addition was successful. `None` if the save operation failed
   */
  def add(shortName: String, addressText: String, country: String): Option[Address] = add(create(shortName, addressText, country))

  /**
   * Create a new `Address` record , but '''does not''' add it to the database
   */
  def create(shortName: String, addressText: String, country: String): Address = {
    Address.createRecord.shortName(shortName).addressText(addressText).country(country)
  }
  /**
   * Add a new address unless a matching record is found.
   *
   * @return `Some(address)` if a match was made, or the addition was successful. `None` if the save operation failed
   */
  def add(address: Address): Option[Address] = findMatching(address) match {
    case Some(addr) => Some(addr)
    case _ => address.save match {
      case a: Address => Some(a)
      case _ => None
    }
  }

  /**
   * Find an `Address` that ''matches'' the provided one
   *
   * An `Address` matches if one of the  following is true:
   *  * There is an address with the same `ObjectId`
   *  * There is a record with the same `short name`
   *  * There is a record with the same `address text`
   *
   * @return An optional `Address` that is populated if a match is found, or `None` otherwise
   */
  def findMatching(address: Address): Option[Address] = findById(address.id.get) match {
    case Some(addr) => Some(addr)
    case _ => Address.or(
      _.where(_.shortName eqs address.shortName.get),
      _.where(_.addressText eqs address.addressText.get)) get
  }
}
