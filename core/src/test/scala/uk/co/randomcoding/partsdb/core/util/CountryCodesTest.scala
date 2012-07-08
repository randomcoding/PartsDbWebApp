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

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

import CountryCodes._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class CountryCodesTest extends FunSuite with ShouldMatchers {
  private val ukCode = CountryCode("UK", "United Kingdom")

  test("Identify Country Code from address lines for UK address") {
    var addressLines = "23 Lane, Addressville, Town, FG9 7DG. United Kingdom".split("""[,\.] """).toList map (_ trim)
    countryCodeFromAddressLines(addressLines) should be(Some(ukCode))

    addressLines = "23 Lane, Addressville, Town, FG9 7DG. UK".split("""[,\.] """).toList map (_ trim)
    countryCodeFromAddressLines(addressLines) should be(Some(ukCode))

    addressLines = "23 Lane, Addressville, Town, United Kingdom".split("""[,\.] """).toList map (_ trim)
    countryCodeFromAddressLines(addressLines) should be(Some(ukCode))

    addressLines = "23 Lane, Addressville, Town, UK".split("""[,\.] """).toList map (_ trim)
    countryCodeFromAddressLines(addressLines) should be(Some(ukCode))
  }

  test("Address lines with no country code fail correctly") {
    val addressLines = "23 Lane, Addressville, Town, 98765".split(", ").toList map (_ trim)
    countryCodeFromAddressLines(addressLines) should be(None)
  }

  test("Identify country code from full country name") {
    matchToCountryCode("United Kingdom") should be(Some(ukCode))
  }

  test("Identify country code from country tag") {
    matchToCountryCode("UK") should be(Some(ukCode))
  }

  test("Identify country code from country tag that does not exist fails correctly") {
    matchToCountryCode("ZZ") should be(None)
  }

  test("Case is not important when matching on country code") {
    matchToCountryCode("uk") should be(Some(ukCode))
    matchToCountryCode("UK") should be(Some(ukCode))
    matchToCountryCode("Uk") should be(Some(ukCode))
    matchToCountryCode("uK") should be(Some(ukCode))
  }

  test("Case is not important when matching on country name") {
    matchToCountryCode("UNITED KINGDOM") should be(Some(ukCode))
    matchToCountryCode("united kingdom") should be(Some(ukCode))
    matchToCountryCode("United Kingdom") should be(Some(ukCode))
    matchToCountryCode("UnItEd KinGdom") should be(Some(ukCode))
  }

  test("First Country Code is returned if address lines contain multiple matches") {
    val addressLines = "23 Lane, Addressville, Town, UK, US".split(", ").toList map (_ trim)
    countryCodeFromAddressLines(addressLines) should be(Some(ukCode))
  }

  test("Country Code Pattern Matching against country tag") {
    val input = List("UK", "uk", "Uk", "uK")
    input foreach { entry =>
      entry match {
        case CountryCode(code) => code should be(ukCode)
        case _ => fail("%s failed to match country code".format(entry))
      }
    }
  }

  test("Country Code Pattern Matching against country name") {
    val input = List("United Kingdom", "united kingdom", "UniTEd kingDom", "UNITED KINGDOM")
    input foreach { entry =>
      entry match {
        case CountryCode(code) => code should be(ukCode)
        case _ => fail("%s failed to match country code".format(entry))
      }
    }
  }

  test("Country Code Pattern Matching against address with country name") {
    val address = List("A House", "A Street", "A Town", "PO51 3DE", "United Kingdom")
    address match {
      case CountryCode(code) => code should be(ukCode)
      case _ => fail("Failed to match address %s to uk code".format(address.mkString(", ")))
    }
  }

  test("Country Code Pattern Matching against address with country tag") {
    val address = List("A House", "A Street", "A Town", "PO51 3DE", "UK")
    address match {
      case CountryCode(code) => code should be(ukCode)
      case _ => fail("Failed to match address %s to uk code".format(address.mkString(", ")))
    }
  }

  test("Country Code Pattern Matching against address with multiple country names") {
    val address = List("A House", "A Street", "A Town", "PO51 3DE", "United Kingdom", "France")
    address match {
      case CountryCode(code) => code should be(ukCode)
      case _ => fail("Failed to match address %s to uk code".format(address.mkString(", ")))
    }
  }

  test("Country Code Pattern Matching against address with multiple country tags") {
    val address = List("A House", "A Street", "A Town", "PO51 3DE", "UK", "US", "FR")
    address match {
      case CountryCode(code) => code should be(ukCode)
      case _ => fail("Failed to match address %s to uk code".format(address.mkString(", ")))
    }
  }

  test("Country Code Pattern Matching against address with no country name or tag") {
    val address = List("A House", "A Street", "A Town", "PO51 3DE")
    address match {
      case CountryCode(code) => fail("Address %s should not match a country code".format(address.mkString(", ")))
      case _ => //test passes
    }
  }

}
