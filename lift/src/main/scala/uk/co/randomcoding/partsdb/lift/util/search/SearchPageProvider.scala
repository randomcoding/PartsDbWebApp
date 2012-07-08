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
package uk.co.randomcoding.partsdb.lift.util.search

import scala.xml.NodeSeq

/**
 * Base class for Search page with their providers.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
abstract class SearchPageProvider {

  val name: String
  val providesType: String

  def renderSearchControls: NodeSeq
}

object CustomerSearchPageProvider extends SearchPageProvider {
  override val name = "Customer"
  override val providesType = "Customer"

  override def renderSearchControls: NodeSeq = <lift:embed what="_customer_search"/>
}

/*object QuoteSearchPageProvider extends SearchPageProvider {
  override val name = "Quote"
  override val providesType = "Quote"

  override def renderSearchControls: NodeSeq = <lift:embed what="_quote_search"/>
}*/
