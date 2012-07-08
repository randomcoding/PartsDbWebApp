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
package uk.co.randomcoding.partsdb.core.address

import uk.co.randomcoding.partsdb.core.util.CountryCodes._
import uk.co.randomcoding.partsdb.core.util.CountryCode

/**
 * Parses address strings into [[uk.co.randomcoding.partsdb.core.address.Address]]es
 *
 * == Example ==
 *
 * This will generate an [[uk.co.randomcoding.partsdb.core.address.Address]] object from the input string
 *
 * {{{
 * val addressText = "4 House Lane, A Village, A Town, DG6 9GH. UK"
 * val address = addressText match {
 *   case AddressParser(a) => a
 *   case _ => NullAddress
 * }
 * }}}
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object AddressParser {
  /**
   * Pattern matching on an input tuple of (short name, address lines, country) that attempts to create an
   * [[uk.co.randomcoding.partsdb.core.address.Address]] from the input.
   *
   * If the first element of the tuple is empty then the first line of the address lines is used for the address's short name
   * The address lines have any trailing commas of full stops removed and are then `trimmed`. Empty lines are then removed.
   * Then the country is matched to a country code in [[uk.co.randomcoding.partsdb.core.util.CountryCodes]].
   *
   * If there is a country code match (either on the country code or full name) and the address lines are not basically empty,
   * then an [[uk.co.randomcoding.partsdb.core.address.Address]] is generated and wrapped in an `Option`
   *
   * @return An Option[[[ uk.co.randomcoding.partsdb.core.address.Address]]] if the input string matches
   */
  def unapply(nameAndAddress: (String, Seq[String], String)): Option[Address] = {
    nameAndAddress match {
      case (_, Nil, _) => None
      case _ => {
        val addressLines = nameAndAddress._2 map (_.replaceAll("""[,\.]$""", "") trim) filter (_ nonEmpty) //addressString.split("""[,\.]+""") map (_ trim)
        val shortName = nameAndAddress._1.trim match {
          case "" => addressLines(0)
          case s => s
        }

        identifyCountry(nameAndAddress._3) match {
          case Some(code) if (addressLines.size >= 1) => Some(Address(shortName, addressLines.mkString("\n"), code.countryName))
          case _ => None
        }
      }
    }
  }

  /**
   * Gets the country code from the address.
   *
   * Currently this will check for a UK Post Code and return UK, otherwise will return the last line of the input list
   *
   * This is the key method as it is used by the match in apply to determine if an address is valid.
   *
   * This needs a better means of country identification
   */
  private def identifyCountry(country: String): Option[CountryCode] = matchToCountryCode(country)
}
