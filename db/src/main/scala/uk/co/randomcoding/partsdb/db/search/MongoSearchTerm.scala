/**
 *
 */
package uk.co.randomcoding.partsdb.db.search

import com.mongodb.casbah.Imports._

/**
 * MongoDB implementtion of  search term.
 *
 * @constructor Create a new instance of a MongoDB compatible search term.
 * This should be suitable for composing into a complex search object
 *
 * @param searchKey The value key to search for in the queries Mongo Collection
 * @param searchValue The value to limit the returned documents by. If this is '''not'''
 * a value from the predefined types in [[uk.co.randomcoding.partsdb.db.search.SearchTerm]] then
 * it is used exactly as entered and will generate a query `(searchKey -> searchValue)`
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
case class MongoSearchTerm(searchKey: String, searchValue: String) extends SearchTerm(searchKey, searchValue) {
  type QueryType = DBObject

  override val query = searchValue match {
    case SearchTerm.exists => (searchKey $exists true)
    case SearchTerm.doesNotExist => (searchKey $exists false)
    case other => MongoDBObject(searchKey -> searchValue)
  }
}