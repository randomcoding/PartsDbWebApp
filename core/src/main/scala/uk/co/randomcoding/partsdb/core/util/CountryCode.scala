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
package uk.co.randomcoding.partsdb.core.util

class CountryCode(val tag: String, val countryName: String) {
  override def toString = "Country Code: [tag: %s, countryName: %s]".format(tag, countryName)

  override def equals(that: Any): Boolean = {
    if (that.isInstanceOf[CountryCode]) {
      val other = that.asInstanceOf[CountryCode]
      tag == other.tag && countryName == other.countryName
    }
    else {
      false
    }
  }

  override def hashCode(): Int = {
    getClass.hashCode + tag.hashCode + countryName.hashCode
  }
}
/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object CountryCode {
  def apply(tag: String, countryName: String) = new CountryCode(tag, countryName)

  def apply(codeTuple: (String, String)) = new CountryCode(codeTuple._1, codeTuple._2)

  /**
   * Pattern match an address for its country code
   */
  def unapply(addressLines: Seq[String]): Option[CountryCode] = CountryCodes.countryCodeFromAddressLines(addressLines)

  /**
   * Pattern match a string against a country code
   */
  def unapply(countryNameOrTag: String): Option[CountryCode] = CountryCodes.matchToCountryCode(countryNameOrTag)
}
