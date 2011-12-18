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
 * @param collection The collection that this search provider should use
 * @tparam ResultType The type of results that this provider returns from its searches
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
sealed abstract class MongoSearchProvider[ResultType](val name: String, val providesType: String, coll: MongoCollection)(implicit mf: Manifest[ResultType]) {

  val mongoAccess = new MongoAllOrOneAccess {
    override val collection = coll
  }
  /**
   * A String representation of the type of entity this provider returns.
   *
   * e.g. `Customer` or `Quote`
   */

  def query(searchTerms: Set[MongoSearchTerm]): MongoDBObject = {
    (searchTerms.toList match {
      case Nil => MongoDBObject.empty
      case head :: Nil => head.query
      case multiple => multiple.foldLeft(MongoDBObject.empty)((currentQuery: DBObject, term: MongoSearchTerm) => currentQuery ++ term.query)
    }) ++ ("addressId" $exists true)
  }
  /**
   * Perform the search and get the results form the datastore
   *
   * @param searchTerms A set of distinct [[uk.co.randomcoding.partsdb.db.search.SearchTerm]]s that will be used to get the results of the search
   */
  def search(searchTerms: Set[MongoSearchTerm]): List[ResultType] = mongoAccess.getMatching[ResultType](query(searchTerms))
}

// add implementations
case class AddressSearchProvider(collection: MongoCollection) extends MongoSearchProvider[Address]("Address Search", "Address", collection)

case class CustomerSearchProvider(collection: MongoCollection) extends MongoSearchProvider[Customer]("Customer Search", "Customer", collection)

case class PartSearchProvider(collection: MongoCollection) extends MongoSearchProvider[Part]("Part Search", "Part", collection)

//case class SupplierSearchProvider(collection: MongoCollection) extends MongoSearchProvider[Supplier]("Supplier Search", "Supplier", collection)

//case class QuoteSearchProvider(collection: MongoCollection) extends MongoSearchProvider[Quote]("Quote Search", "Quote", collection)