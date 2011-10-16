/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import scala.collection.mutable.{ Map => MMap }

import com.mongodb.casbah.Imports._

import uk.co.randomcoding.partsdb.core.address.{ AddressId, Address }
import uk.co.randomcoding.partsdb.db.DbAccess
import uk.co.randomcoding.partsdb.db.mongo.MongoConvertors._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
class MongoDbAccess(mongoCollection: MongoCollection) extends DbAccess {

  override def addresses: Set[Address] = {
    val query = "addressId" $exists true

    val results = for (result <- mongoCollection.find(query).toSet[DBObject]) yield {
      val address: Address = result
      address
    }
    println("Got addresses: %s".format(results.mkString("[", ", ", "]")))

    results
  }

  override def address(id: AddressId): Address = {
    val query = MongoDBObject("addressId" -> "{\"id\" = %d }".format(id.id))
    val addresses = for (result <- mongoCollection.findOne(query).toList) yield {
      val address: Address = result
      address
    }

    addresses.head
  }
}

object MongoDbAccess {
  import scala.collection.mutable.{ Map => MMap }

  private val collections = MMap.empty[String, MongoCollection].withDefault(key => {
    val parts = key.split(":")
    MongoConfig.getCollection(parts(1), parts(2))
  })

  def apply(dbName: String, collectionName: String): MongoCollection = collections("%s:%s".format(dbName, collectionName))
}