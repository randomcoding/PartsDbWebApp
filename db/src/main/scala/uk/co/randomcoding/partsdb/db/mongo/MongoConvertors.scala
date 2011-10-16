/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import com.mongodb.casbah.Imports._
import uk.co.randomcoding.partsdb.core.address.{ Address, AddressId }

/**
 * Provides implicit conversions between DBObjects and library objects
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
object MongoConvertors {
  // TODO: These should use JSON conversions rather than explicitly creating objects.
  implicit def addressToMongo(address: Address): DBObject = {
    val idObject = MongoDBObject("id" -> address.id.id)
    MongoDBObject("id" -> idObject,
      "shortName" -> address.shortName,
      "addressText" -> address.addressText,
      "country" -> address.country)
  }

  implicit def mongoToAddress(mongoAddress: DBObject): Address = {
    val id: AddressId = mongoAddress.as[AddressId]("id")
    Address(id, mongoAddress.as[String]("shortName"),
      mongoAddress.as[String]("addressText"),
      mongoAddress.as[String]("country"))
  }
}