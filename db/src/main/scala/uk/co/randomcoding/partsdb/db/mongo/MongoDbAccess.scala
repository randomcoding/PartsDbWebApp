/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import scala.collection.mutable.{ Map => MMap }
import com.mongodb.casbah.Imports._
import uk.co.randomcoding.partsdb.db.DbAccess
import uk.co.randomcoding.partsdb.db.mongo.MongoConverters._
import net.liftweb.common.Logger

/**
 * Provides access to stored items in a MongoDB Collection
 *
 * @constructor Creates a new instance of this access object. Should be called from the companion object
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
class MongoDbAccess(override val collection: MongoCollection) extends MongoAllOrOneAccess with MongoIdentifierAccess with MongoUpdateAccess with Logger {
  //override val collection = mongoCollection

  /*override def add[T <: AnyRef](t: T): Unit = {
    val dbo: DBObject = t
    mongoCollection += dbo
  }*/

  /*private val incrementUniqueId = (currentId: Long) => {
    val newIdObject = MongoDBObject("uniqueId" -> (currentId + 1))
    val findIdObjectQuery = "uniqueId" $exists true
    mongoCollection.findOne(findIdObjectQuery) match {
      case None => mongoCollection += newIdObject
      case _ => mongoCollection.findAndModify(findIdObjectQuery, newIdObject)
    }
  }

  val idQuery = "uniqueId" $exists true

  */
  /**
   * Gets the next value for the unique id.
   *
   * This also increments the current value that is stored in the database
   */ /*
  override def nextId(): Long = {
    val findOneQuery = mongoCollection.findOne(idQuery)
    val idValue = findOneQuery match {
      case None => {
        0
      }
      case Some(v) => {
        v.as[Long]("uniqueId")
      }
    }
    incrementUniqueId(idValue)
    idValue
  }

  private val addressQuery = (addressId: AddressId) => MongoDBObject("addressId" -> MongoDBObject("id" -> addressId.id))

  private val addressesQuery = () => "addressId" $exists true

  override def addresses: Set[Address] = {
    val results = for (result <- mongoCollection.find(addressesQuery()).toSet[DBObject]) yield {
      val address: Address = convertFromMongoDbObject[Address](result)
      address
    }
    debug("Got addresses: %s".format(results.mkString("[", ", ", "]")))

    results
  }

  override def address(id: AddressId): Option[Address] = {
    val addresses = for (result <- mongoCollection.findOne(addressQuery(id)).toList) yield {
      val address: Address = convertFromMongoDbObject[Address](result)
      address
    }

    addresses match {
      case Nil => None
      case head :: Nil => Some(head)
      case head :: _ => {
        warn("Query for address with id %s returned %d results:\n%s.\n\nReturning first value as result".format(id, addresses.size, addresses.mkString("[", ", ", "]")))
        Some(head)
      }
    }
  }*/
}

/**
 * Factory for [[uk.co.randomcoding.partsdb.db.mongo.MongoDBAccess]] instances
 *
 * == Sample ==
 * {{{
 * val access = MongoDBAccess("test-db", "test-collection")
 * }}}
 * Will create an instance of the access class using the collection '''test-collection''' from the database '''test-db'''.
 *
 * If the database and/or collection do not exist, then I believe MongoDB will create them for you.
 */
object MongoDbAccess {
  import scala.collection.mutable.{ Map => MMap }

  private val accessObjects = MMap.empty[String, MongoDbAccess].withDefault(key => {
    val parts = key.split(":")
    new MongoDbAccess(MongoConfig.getCollection(parts(0), parts(1)))
  })

  /**
   * Get the instance of an [[uk.co.randomcoding.partsdb.db.mongo.MongoDBAccess]] object for the given collection within the database
   *
   * @param dbName The name of the database to connect to
   * @param collectionName The name of the collection to connect to
   * @return The instance of the MongoDbAccess object for the given collection and database
   */
  def apply(dbName: String, collectionName: String): MongoDbAccess = accessObjects("%s:%s".format(dbName, collectionName))
}