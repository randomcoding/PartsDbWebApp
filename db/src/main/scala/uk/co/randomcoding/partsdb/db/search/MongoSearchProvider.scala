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
import uk.co.randomcoding.partsdb.core.id.Identifier

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
  type SearchTermType <: MongoSearchTerm
  /**
   * @abstract
   * The type of results that this provider returns from its searches
   */
  type ResultType

  type QueryType = DBObject

  val mongoAccess = new MongoAllOrOneAccess {
    override val collection = coll
  }

  def genQuery(term: MongoSearchTerm) = term.query

  /**
   * Generates the Mongo DB query represented by the provided [[uk.co.randomcoding.partsdb.db.search.MongoSearchTerm]]s
   *
   * This provides '''and''' style searches.
   *
   * @return A `MongoDBObject` that is the concatenation of all the search terms. If the input set is empty then returns `MongoDBObject.empty`
   */
  private def query[T <: MongoSearchTerm](searchTerms: Set[T]): QueryType = {
    (searchTerms.toList match {
      case Nil => MongoDBObject.empty
      case head :: Nil => genQuery(head)
      case multiple => multiple.foldLeft(MongoDBObject.empty)((currentQuery: DBObject, term: MongoSearchTerm) => currentQuery ++ genQuery(term))
    }) ++ typeIdQuery
  }
  /**
   * Perform the search and get the results form the datastore
   *
   * @param searchTerms A set of distinct [[uk.co.randomcoding.partsdb.db.search.SearchTerm]]s that will be used to get the results of the search
   */
  protected def search[ResultType, T <: MongoSearchTerm](searchTerms: Set[T])(implicit mf: Manifest[ResultType]): List[ResultType] = mongoAccess.getMatching[ResultType](query(searchTerms))

  /**
   * Type fixed method to call to perform the search
   */
  def find[T <: MongoSearchTerm](searchTerms: Set[T]): List[ResultType]

  /**
   * Delegate method to `find(Set)` that wrape a single term in a Set
   *
   * This should follow a standard pattern:
   * {{{
   * def find(searchTerms: Set[MongoSearchTerms]): List[ResultType] = search[ResultType](searchTerms)
   * }}}
   */
  def find[T <: MongoSearchTerm](searchTerm: T): List[ResultType] = find(Set(searchTerm))

  /**
   * This is the query to limit the results by the type's main id field.
   *
   * e.g. `addressId` for addresses or `partId` for parts.
   *
   * This will generally be of the form
   * {{{
   * override val typeIdQuery = ("addressId" $exists true)
   * }}}
   *
   * If the searched entity type does not have an id field as such simply use `MongoDBObject.empty`
   */
  val typeIdQuery: QueryType
}

// add implementations
case class AddressSearchProvider(collection: MongoCollection) extends MongoSearchProvider("Address Search", "Address", collection) {

  override type ResultType = Address

  override def find[T <: MongoSearchTerm](searchTerms: Set[T]): List[Address] = search[Address, T](searchTerms)

  override val typeIdQuery = ("addressId" $exists true)

}

case class CustomerSearchProvider(collection: MongoCollection) extends MongoSearchProvider("Customer Search", "Customer", collection) {
  override type ResultType = Customer

  override def find[T <: MongoSearchTerm](searchTerms: Set[T]): List[Customer] = search[Customer, T](searchTerms)

  override val typeIdQuery = ("customerId" $exists true)

  override def genQuery(term: MongoSearchTerm) = {
    term.searchKey match {
      case key: String if (key startsWith "billingAddress.") && !(key contains ".id") => {
        val newKey = key.drop(key.indexOf(".") + 1)
        val addressTerm = MongoSearchTerm(newKey, term.searchValue)
        val addressQuery = ("addressId" $exists true) ++ addressTerm.query
        val fields = MongoDBObject("addressId" -> 1)
        val addressIds = collection.find(addressQuery, fields).toList map (dbo => ("billingAddress.id", dbo.as[DBObject]("addressId").as[Long]("id"))) distinct

        $or(addressIds: _*)
      }
      case _ => super.genQuery(term)
    }
  }
}

case class PartSearchProvider(collection: MongoCollection) extends MongoSearchProvider("Part Search", "Part", collection) {
  override type ResultType = Part

  override def find[T <: MongoSearchTerm](searchTerms: Set[T]): List[Part] = search[Part, T](searchTerms)

  override val typeIdQuery = ("partId" $exists true)
}

case class QuoteSearchProvider(collection: MongoCollection) extends MongoSearchProvider("Quote Search", "Quote", collection) {
  override type ResultType = Document

  override def find[T <: MongoSearchTerm](searchTerms: Set[T]): List[Document] = search[Document, T](searchTerms)

  override val typeIdQuery = MongoDBObject("documentType" -> "QUO") ++ ("documentId" $exists true)

  override def genQuery(term: MongoSearchTerm) = {
    term.searchKey match {
      case SearchKeys.quotePartName => {
        val parts = mongoAccess.getMatching[Part](("partId" $exists true) ++ MongoDBObject("partName" -> term.searchValue))
        val ids = parts.toList map (doc => ("partId.id", doc.partId.id)) distinct

        $or(ids: _*)
      }
      case _ => super.genQuery(term)
    }
  }
}

// The requires types for these providers are not implemented yet
/*case class SupplierSearchProvider(collection: MongoCollection) extends MongoSearchProvider("Supplier Search", "Supplier", collection) {
  override type ResultType = Supplier
}

case class QuoteSearchProvider(collection: MongoCollection) extends MongoSearchProvider("Quote Search", "Quote", collection){
  override type ResultType = Quote
}*/ 