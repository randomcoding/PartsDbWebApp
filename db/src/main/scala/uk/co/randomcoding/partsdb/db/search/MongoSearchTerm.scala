/**
 *
 */
package uk.co.randomcoding.partsdb.db.search

import scala.util.matching.Regex

/**
 * MongoDB implementtion of  search term.
 *
 * @constructor Create a new instance of a MongoDB compatible search term.
 * This should be suitable for composing into a complex search object
 *
 * To search a nested property
 *
 * @param searchKey The value key to search for in the queries Mongo Collection
 * @param searchValue The value to limit the returned documents by. If this is '''not'''
 * a value from the predefined types in [[uk.co.randomcoding.partsdb.db.search.SearchTerm]] then
 * it is used exactly as entered and will generate a query `(searchKey -> searchValue)`
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
sealed abstract class MongoSearchTerm(val searchKey: String) extends SearchTerm(searchKey) {
  override type QueryType = (String, valueType)

  override val query = searchValue match {
    //case SearchTerm.exists => (searchKey $exists true)
    //case SearchTerm.doesNotExist => (searchKey $exists false)
    case other => genQuery
  }

  def genQuery = (searchKey -> searchValue)
}

case class StringSearchTerm(key: String, override val searchValue: String) extends MongoSearchTerm(key) {
  override type valueType = String
}

case class RegexSearchTerm(key: String, override val searchValue: Regex) extends MongoSearchTerm(key) {
  override type valueType = Regex
}

case class IntegerSearchTerm(key: String, override val searchValue: Int) extends MongoSearchTerm(key) {
  override type valueType = Int
}

case class DoubleSearchTerm(key: String, override val searchValue: Double) extends MongoSearchTerm(key) {
  override type valueType = Double
}

/*case class ArrayStringSearchTerm(key: String, override val searchValue: String) extends MongoSearchTerm(key) {
	override type valueType = String
	
	def genQuery = MongoDBObject
}

case class RegexSearchTerm(key: String, override val searchValue: Regex) extends MongoSearchTerm(key) {
	override type valueType = Regex
}

case class IntegerSearchTerm(key: String, override val searchValue: Int) extends MongoSearchTerm(key) {
	override type valueType = Int
}

case class DoubleSearchTerm(key: String, override val searchValue: Double) extends MongoSearchTerm(key) {
	override type valueType = Double
}*/

object MongoSearchTerm {
  /**
   * Generates a typed search term from the given key and value.
   *
   * If the value is not a String, Int or Double then it is converted into a
   * `StringSearchTerm` by `searchValue.toString`
   */
  def apply(searchKey: String, searchValue: Any): MongoSearchTerm = {
    searchValue match {
      case s: String => StringSearchTerm(searchKey, s)
      case i: Int => IntegerSearchTerm(searchKey, i)
      case d: Double => DoubleSearchTerm(searchKey, d)
      case r: Regex => RegexSearchTerm(searchKey, r)
      case other => StringSearchTerm(searchKey, other.toString)
    }
  }
}