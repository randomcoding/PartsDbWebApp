/**
 *
 */
package uk.co.randomcoding.partsdb.db.search

/**
 * A term that is used to search the datastore for information.
 *
 * It is used to convert between a general search term and the actual query to perform
 * on the underlying datastore.
 *
 * This is a document oriented feature, but should be easily mappable to a SQL style if required.
 *
 * This is essentially a key value pair where the value is used as the filter on the permitted values for the key.
 *
 * There are certain predefined values for the `searchValue` that have specific behaviours. These are defined in the `SearchTerm` companion object.
 *  - '''exists''': The key simply has to exist in the returned entity, any value is ok.
 *  - '''
 *
 * @param searchKey
 * @param searchValue
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
abstract class SearchTerm(searchKey: String) {
  type valueType

  val searchValue: valueType
  /**
   * The type of object that represents the Query
   *
   * e.g. For MongoDB this will be a `MongoDBObject` or `DBObject`
   */
  type QueryType

  /**
   * The datastore specific query that will peform the search
   */
  val query: QueryType
}

/**
 * Provides predefined `searchValues` that can be used in a `SearchTerm`
 */
object SearchTerm {
  /**
   * The key for this value only has to exist or have a value (dependent on datastore implementation)
   */
  val exists = "exists"

  /**
   * The key for this value must not exist or have an empty value (dependent on datastore implementation)
   */
  val doesNotExist = "not exists"

  // TODO add bridges to other Casbah Query DSL types as needed (e.g. greater than, range etc.)
}