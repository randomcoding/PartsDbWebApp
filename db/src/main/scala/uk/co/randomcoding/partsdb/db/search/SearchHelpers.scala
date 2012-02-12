/**
 *
 */
package uk.co.randomcoding.partsdb.db.search

import java.util.regex.Pattern
import com.foursquare.rogue.EqClause
import com.foursquare.rogue.DocumentScan
import scala.util.matching.Regex

/**
 * Common functions to make generation of search queries easier
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object SearchHelpers {

  /**
   * Create a case insensitive pattern to match on the existence of the provided string within a value (i.e. substring match)
   */
  def regexValue(subString: String): Pattern = Pattern.compile(".*%s.*".format(subString), Pattern.CASE_INSENSITIVE)

  /**
   * Generate a regex match query.
   *
   * @param fieldName The name of the field to query. Case Sensitive.
   * @param matchString The string to match within the field's value
   */
  def regexTerm(fieldName: String, matchString: String) = new EqClause[Pattern, DocumentScan](fieldName, DocumentScan, ".*%s.*".format(matchString.trim).r.pattern)
}