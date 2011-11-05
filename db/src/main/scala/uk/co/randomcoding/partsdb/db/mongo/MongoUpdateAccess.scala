/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import com.mongodb.casbah.Imports._
import uk.co.randomcoding.partsdb.db.mongo._
import MongoConverters._
import MongoAccessHelpers._

/**
 * Provides an implementation of the capability to add an object to a collection
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
trait MongoUpdateAccess {
  /**
   * The `MongoCollection` this trait will operate upon
   */
  val collection: MongoCollection

  /**
   * Adds an object into the database if there is not already another item with the same [[uk.co.randomcoding.partsdb.core.id.Identifier]].
   *
   * @return `true` iff there was no other object with the same [[uk.co.randomcoding.partsdb.core.id.Identifier]] and
   * 	the add operation resulted in there being an object with the new [[uk.co.randomcoding.partsdb.core.id.Identifier]] in the db
   */
  def add[T <: AnyRef](t: T)(implicit mf: Manifest[T]): Boolean = if (idNotInDb[T](t, collection)) addAndVerify[T](t) else false

  private def addAndVerify[T <: AnyRef](t: T)(implicit mf: Manifest[T]): Boolean = {
    collection += t
    idIsInDb[T](t, collection)
  }
}