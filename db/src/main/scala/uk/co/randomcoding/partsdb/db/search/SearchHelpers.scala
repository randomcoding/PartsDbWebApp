/*
 * Copyright (C) 2012 RandomCoder <randomcoder@randomcoding.co.uk>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *    RandomCoder - initial API and implementation and/or initial documentation
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
