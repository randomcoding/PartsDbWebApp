/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet.search

import net.liftweb.http.js.JsCmd

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait SearchSnippet {
  /*val searchKeys: List[String]
  */
  /**
   * Helper function to generate a regex search term
   */ /*
  final def regexTerm(key: String, subString: String) = MongoSearchTerm(key, ".*%s.*".format(subString.trim).r)

  */
  /**
   * Generates a list of unique, non empty, [[uk.co.randomcoding.partsdb.db.search.MongoSearchTerm]]s
   * used to find the matching enreies in the database,
   */ /*
  final def searchTerms = searchKeys map { key =>
    key match {
      case SearchKeys.quotePartName => termForKey(key)
    }
  } filterNot (_.searchValue.toString == ".*.*")

  def termForKey(key: String): MongoSearchTerm
  */
  /**
   * Convenience function to update the value of a variable and then return a partial function of `updateResults(String)`
   */ /*
  final def updateValue(func: () => Any): (String) => JsCmd = {
    func()
    updateResults(_: String)
  }

  def updateResults(s: String = "")*/
}