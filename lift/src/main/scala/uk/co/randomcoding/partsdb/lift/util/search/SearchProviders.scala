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
package uk.co.randomcoding.partsdb.lift.util.search

import scala.collection.mutable.{ Set => MSet }

/**
 * Provides access to all the search providers that are available.
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object SearchProviders {
  private val searchProviders: MSet[SearchPageProvider] = MSet.empty

  /**
   * Register a [[uk.co.randomcoding.partsdb.db.search.SearchProvider]] for use with the WebApp
   */
  def register(provider: SearchPageProvider): Unit = searchProviders += provider

  /**
   * Get the registered search providers, sorted by their name
   */
  def providers = searchProviders.toList sortBy (_.name)

  def providerFor(providesType: String) = searchProviders.find(_.providesType == providesType)
}
