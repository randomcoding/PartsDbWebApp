/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import com.mongodb.casbah.Imports._
import uk.co.randomcoding.partsdb.db.mongo.MongoConverters._

/**
 * Provides an implementation of the capability to add an object to a collection
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
trait MongoUpdateAccess {
  val collection: MongoCollection

  def add[T <: AnyRef](t: T): Unit = {
    val dbo: DBObject = t
    collection += dbo
  }
}