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
}

object Address extends Address with MongoMetaRecord[Address] {
  import org.bson.types.ObjectId
  
  def findById(oid: ObjectId) = Address where (_.id eqs oid) get
  
  def findByShortName(shortName: String) = Address where (_.shortName eqs shortName) fetch
  
  def findByAddressText(addressText: String) = Address where (_.addressText eqs addressText) fetch
  
  def remove(shortName: String) = findByShortName(shortName) map (_.delete_!) distinct
  
  def modify(oid: ObjectId, newShortName: String, newAddressText: String, newCountry: String) = {
    Address where (_.id eqs oid) modify (_.shortName setTo newShortName) and (_.addressText setTo newAddressText) and (_.country setTo newCountry) updateMulti
  }
  
  def add(shortName: String, addressText: String, country: String) = Address.createRecord.shortName(shortName).addressText(addressText).country(country).save
}
