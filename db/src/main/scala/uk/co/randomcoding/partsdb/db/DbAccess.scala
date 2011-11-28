/**
 *
 */
package uk.co.randomcoding.partsdb.db
import uk.co.randomcoding.partsdb.db.mongo.{ MongoUpdateAccess, MongoIdentifierAccess, MongoConfig, MongoAllOrOneAccess }

/**
 * Encapsulates all the Database access functionality in a single class
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 * @constructor Create a new instance of the Database access
 * @param dbName The name of the database to connect to
 * @param collectionName The name of the Collection to get from the database
 */
class DbAccess(dbName: String, collectionName: String) extends MongoIdentifierAccess with MongoUpdateAccess with MongoAllOrOneAccess {
  override lazy val collection = MongoConfig.getCollection(dbName, collectionName)
}