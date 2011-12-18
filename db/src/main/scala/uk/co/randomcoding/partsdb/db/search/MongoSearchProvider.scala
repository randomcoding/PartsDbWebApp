/**
 *
 */
package uk.co.randomcoding.partsdb.db.search

import com.mongodb.casbah.Imports._
import uk.co.randomcoding.partsdb.core._
import address.Address
import customer.Customer
import part.Part
import document._
import uk.co.randomcoding.partsdb.db.mongo.MongoAllOrOneAccess

/**
 * Provides access to structured searches (via [[uk.co.randomcoding.partsdb.db.search.SearchTerm]]s) on the underlying datastore.
 *
 * @constructor Create a new search provider
 * @param name The name of the search provider.
 * @param providesType A String representation of the type of entity this provider returns
 * @param collection The collection that this search provider should use
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
sealed abstract class MongoSearchProvider(val name: String, val providesType: String, coll: MongoCollection) {
  /**
   * @abstract
   * The type of results that this provider returns from its searches
   */
  type ResultType

  private[this] val mongoAccess = new MongoAllOrOneAccess {
    override val collection = coll
  }

  /**
   * Generates the Mongo DB query represented by the provided [[uk.co.randomcoding.partsdb.db.search.MongoSearchTerm]]s
   *
   * This provides '''and''' style searches.
   *
   * @return A `MongoDBObject` that is the concatenation of all the search terms. If the input set is empty then returns `MongoDBObject.empty`
   */
  private def query(searchTerms: Set[MongoSearchTerm]): MongoDBObject = {
    (searchTerms.toList match {
      case Nil => MongoDBObject.empty
      case head :: Nil => head.query
      case multiple => multiple.foldLeft(MongoDBObject.empty)((currentQuery: DBObject, term: MongoSearchTerm) => currentQuery ++ term.query)
    }) /*++ ("addressId" $exists true)*/
  }
  /**
   * Perform the search and get the results form the datastore
   *
   * @param searchTerms A set of distinct [[uk.co.randomcoding.partsdb.db.search.SearchTerm]]s that will be used to get the results of the search
   */
  protected def search[ResultType](searchTerms: Set[MongoSearchTerm])(implicit mf: Manifest[ResultType]): List[ResultType] = mongoAccess.getMatching[ResultType](query(searchTerms))

  /**
   * Type fixed method to call to perform the search
   */
  def find(searchTerms: Set[MongoSearchTerm]): List[ResultType]

  /**
   * Delegate method to `find(Set)` that wrape a single term in a Set
   */
  def find(searchTerm: MongoSearchTerm): List[ResultType] = find(Set(searchTerm))
}

// add implementations
case class AddressSearchProvider(collection: MongoCollection) extends MongoSearchProvider("Address Search", "Address", collection) {
  override type ResultType = Address

  def find(searchTerms: Set[MongoSearchTerm]): List[Address] = search[Address](searchTerms)
}

case class CustomerSearchProvider(collection: MongoCollection) extends MongoSearchProvider("Customer Search", "Customer", collection) {
  override type ResultType = Customer

  def find(searchTerms: Set[MongoSearchTerm]): List[Customer] = search[Customer](searchTerms)
}

case class PartSearchProvider(collection: MongoCollection) extends MongoSearchProvider("Part Search", "Part", collection) {
  override type ResultType = Part

  def find(searchTerms: Set[MongoSearchTerm]): List[Part] = search[Part](searchTerms)
}

// The requires types for these providers are not implemented yet
/*case class SupplierSearchProvider(collection: MongoCollection) extends MongoSearchProvider("Supplier Search", "Supplier", collection) {
  override type ResultType = Supplier
}

case class QuoteSearchProvider(collection: MongoCollection) extends MongoSearchProvider("Quote Search", "Quote", collection){
  override type ResultType = Quote
}*/ 