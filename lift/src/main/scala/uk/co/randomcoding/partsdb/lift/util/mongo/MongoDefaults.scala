/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.mongo
import uk.co.randomcoding.partsdb.db.mongo.MongoConfig

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object MongoDefaults {
  val defaultDbName = "MainDb"
  val defaultCollectionName = "MainCollection"
  val defaultCollection = MongoConfig.getCollection(defaultDbName, defaultCollectionName)
}