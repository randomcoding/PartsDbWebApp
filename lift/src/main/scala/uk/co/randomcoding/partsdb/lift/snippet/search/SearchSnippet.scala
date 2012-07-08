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
