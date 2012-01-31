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
  def modify(oid: ObjectId, newShortName: String, newAddressText: String, newCountry: String) = {
    Address.where(_.id eqs oid).modify(_.shortName setTo newShortName) and (_.addressText setTo newAddressText) and (_.country setTo newCountry) updateMulti
  }

  /**
   * Add a new Address unless there is an address with the same `shortName` or `addressText` in the database
   *
   * @return An `Option[Address]`, populated with the new `Address` if it was added successfully, or None if it was not.
   */
  def add(shortName: String, addressText: String, country: String) = (findNamed(shortName) ++ findByAddressText(addressText)) match {
    case Nil => {
      Address.createRecord.shortName(shortName).addressText(addressText).country(country).save match {
        case addr: Address => Some(addr)
        case _ => None
      }
    }
    case _ => None
  }

}
