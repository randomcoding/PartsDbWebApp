/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.mongo
import uk.co.randomcoding.partsdb.db.mongo.MongoConfig

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
@deprecated("No longer in use", "0.1")
object MongoDefaults {
  val defaultDbName = "MainDb"
  val defaultCollectionName = "MainCollection"
  val defaultCollection = "Collection" //MongoConfig.getCollection(defaultDbName, defaultCollectionName)
}