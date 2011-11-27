/**
 *
 */
package uk.co.randomcoding.partsdb.core.util

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

import CountryCodes._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class CountryCodesTest extends FunSuite with ShouldMatchers {
  private val ukCode = ("UK", "United Kingdom")

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

}