/**
 *
 */
package uk.co.randomcoding.partsdb.core.address

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import uk.co.randomcoding.partsdb.core.id.DefaultIdentifier

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddressParserTest extends FunSuite with ShouldMatchers {
  test("Test can match a UK address string with lines separated by commas and new lines") {
    val shortName = "Holly Lane"
    val addressString = "15 Holly Lane,\n" +
      "A Town,\n" +
      "A County,\n" +
      "AC23 8FD"

    val expectedAddress = List("15 Holly Lane", "A Town", "A County", "AC23 8FD").mkString("\n")

    (shortName, addressString, "UK") match {
      case AddressParser(addr) => verifyDefaultAddress(expectedAddress, addr)
      case _ => fail("No Address Match made")
    }
  }

  test("Test can match a UK address string with lines separated by both full stops and commas and new lines") {
    val shortName = "Holly Lane"
    val addressString = "15 Holly Lane,\n" +
      "A Town,\n" +
      "A County,\n" +
      "AC23 8FD."

    val expectedAddress = List("15 Holly Lane", "A Town", "A County", "AC23 8FD").mkString("\n")
    (shortName, addressString, "UK") match {
      case AddressParser(addr) => verifyDefaultAddress(expectedAddress, addr)
      case _ => fail("No Address Match made")
    }
  }

  test("Parser correctly uses the first line of the address as short name if the short name is empty") {
    val addressString = """15 Holly Lane,A Town,A County,AC23 8FD."""
    val expectedAddress = List("15 Holly Lane", "A Town", "A County", "AC23 8FD").mkString("\n")

    ("", addressString, "UK") match {
      case AddressParser(addr) => verifyNamedAddress(expectedAddress, addr, "15 Holly Lane")
      case _ => fail("No Address Match made")
    }
  }

  test("Parser correctly uses the first line of the address as short name if the short name is only made up of spaces") {
    val addressString = """15 Holly Lane,A Town,A County,AC23 8FD."""
    val expectedAddress = List("15 Holly Lane", "A Town", "A County", "AC23 8FD").mkString("\n")

    ("   ", addressString, "UK") match {
      case AddressParser(addr) => verifyNamedAddress(expectedAddress, addr, "15 Holly Lane")
      case _ => fail("No Address Match made")
    }
  }

  test("Parser correctly uses the first line of the address as short name if the short name is made up of spaces, tabs and new lines") {
    val addressString = """15 Holly Lane,A Town,A County,AC23 8FD."""
    val expectedAddress = List("15 Holly Lane", "A Town", "A County", "AC23 8FD").mkString("\n")

    (" \t  \n  ", addressString, "UK") match {
      case AddressParser(addr) => verifyNamedAddress(expectedAddress, addr, "15 Holly Lane")
      case _ => fail("No Address Match made")
    }
  }

  test("Test can match a UK address string with all address lines on the same line separated only by full stops and commas (no spaces)") {
    val shortName = "Holly Lane"
    val addressString = """15 Holly Lane,A Town,A County,AC23 8FD."""

    val expectedAddress = List("15 Holly Lane", "A Town", "A County", "AC23 8FD").mkString("\n")
    (shortName, addressString, "UK") match {
      case AddressParser(addr) => verifyDefaultAddress(expectedAddress, addr)
      case _ => fail("No Address Match made")
    }
  }

  test("Test can match a UK address string with all address lines on the same line separated only by full stops and commas with trailing spaces") {
    val shortName = "Holly Lane"
    val addressString = """15 Holly Lane, A Town, A County, AC23 8FD."""

    val expectedAddress = List("15 Holly Lane", "A Town", "A County", "AC23 8FD").mkString("\n")
    (shortName, addressString, "UK") match {
      case AddressParser(addr) => verifyDefaultAddress(expectedAddress, addr)
      case _ => fail("No Address Match made")
    }
  }

  test("Address with only a country code fails to generate address") {
    ("Short", "", "UK") match {
      case AddressParser(addr) => fail("No address text should not generate an address")
      case _ => //passed
    }
  }

  test("Address with out a country code fails to generate address") {
    ("Short", "Address Text", "") match {
      case AddressParser(addr) => fail("No country code should not generate an address")
      case _ => //passed
    }
  }

  private val addressStringMatch = (addressString: String) => {
    val shortName = "Holly Lane"
    val expectedAddress = List("15 Holly Lane", "A Town", "A County", "AC23 8FD").mkString("\n")
    (shortName, addressString, "UK") match {
      case AddressParser(addr) => verifyDefaultAddress(expectedAddress, addr)
      case _ => fail("No Address Match made")
    }
  }

  private val verifyAddress = (expectedAddress: String, addr: Address, shortName: String, country: String) => {
    addr.shortName.get should be(shortName)
    addr.addressText.get should be(expectedAddress)
    addr.country.get should be(country)
  }

  private val verifyNamedAddress = verifyAddress(_: String, _: Address, _: String, "United Kingdom")

  private val verifyDefaultAddress = verifyAddress(_: String, _: Address, "Holly Lane", "United Kingdom")
}