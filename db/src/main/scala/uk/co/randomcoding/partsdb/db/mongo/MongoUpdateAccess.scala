/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import com.mongodb.casbah.Imports._
import uk.co.randomcoding.partsdb.db.mongo._
import MongoConverters._
import MongoAccessHelpers._
import uk.co.randomcoding.partsdb.core.id.Identifiable

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
   * In order to be added, the `new Entry` '''''must''''' have an [[uk.co.randomcoding.partsdb.core.id.Identifier]] variable that has a name ending with ''Id''.
   *
   * @todo Make the type be `[T <: Identifiable]` where `Identifiable` is used to guarantee the identifier field.
   *
   * @param newItem The new item to add to the database.
   * @return `true` iff there was no other object with the same [[uk.co.randomcoding.partsdb.core.id.Identifier]] and
   * 	the add operation resulted in there being an object with the new [[uk.co.randomcoding.partsdb.core.id.Identifier]] in the db
   */
  def add[TYPE <: Identifiable](newItem: TYPE)(implicit mf: Manifest[TYPE]): Boolean = {
    if (idNotInDb[TYPE](newItem, collection)) {
      collection += newItem
      idIsInDb[TYPE](newItem, collection)
    }
    else false
  }

  /**
   * Modifies an object in the database to have the values of the `modifiedItem`.
   *
   * The item must already exist in the database (i.e. there is another entry with the same [[uk.co.randomcoding.partsdb.core.id.Identifier]]) already added.
   *
   * @todo Make the type be `[T <: Identifiable]` where `Identifiable` is used to guarantee the identifier field.
   *
   * @param modifiedItem The item with the new values to be added to the database
   * @return `true` iff there is an item in the database with the same identifier and the update operation succeeds.
   */
  def modify[TYPE <: Identifiable](modifiedItem: TYPE)(implicit mf: Manifest[TYPE]): Boolean = {
    if (idIsInDb[TYPE](modifiedItem, collection)) {
      val originalDbEntry = getDbObject(modifiedItem, collection)

      collection.findAndModify(originalDbEntry, modifiedItem)

      collection.findOne(modifiedItem).isDefined
    }
    else false
  }

  /**
   * Removes an item from the database.
   *
   * The item must exist in the database, '''exactly as passed in to `item`''' otherwise it will not be found.
   *
   * @todo Make the type be `[T <: Identifiable]` where `Identifiable` is used to guarantee the identifier field.
   *
   * @param item The item to be removed from the database
   * @return `true` iff The item is removed from the database. If it is not present (no match found) then returns `false`
   */
  def remove[TYPE <: Identifiable](item: TYPE)(implicit mf: Manifest[TYPE]): Boolean = {
    collection.findAndRemove(item) match {
      case None => false
      case Some(removed) => idNotInDb(item, collection)
    }
  }

}