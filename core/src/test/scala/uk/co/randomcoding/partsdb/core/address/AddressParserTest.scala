/**
 *
 */
package uk.co.randomcoding.partsdb.core.address

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddressParserTest extends FunSuite with ShouldMatchers {
  test("Test can match a UK address string with lines separated by commas and new lines") {
    val shortName = "Holly Lane"
    val addressLines = ("15 Holly Lane,\n" +
      "A Town,\n" +
      "A County,\n" +
      "AC23 8FD").split("\n").toSeq

    val expectedAddress = List("15 Holly Lane", "A Town", "A County", "AC23 8FD").mkString("\n")

    (shortName, addressLines, "UK") match {
      case AddressParser(addr) => verifyDefaultAddress(expectedAddress, addr)
      case _ => fail("No Address Match made")
    }
  }

  test("Test can match a UK address string with lines separated by both full stops and commas and new lines") {
    val shortName = "Holly Lane"
    val addressLines = ("15 Holly Lane,\n" +
      "A Town,\n" +
      "A County,\n" +
      "AC23 8FD.").split("\n").toSeq

    val expectedAddress = List("15 Holly Lane", "A Town", "A County", "AC23 8FD").mkString("\n")
    (shortName, addressLines, "UK") match {
      case AddressParser(addr) => verifyDefaultAddress(expectedAddress, addr)
      case _ => fail("No Address Match made")
    }
  }

  test("Parser correctly uses the first line of the address as short name if the short name is empty") {
    val addressLines = List("15 Holly Lane,", "A Town,", "A County,", "AC23 8FD.")
    val expectedAddress = List("15 Holly Lane", "A Town", "A County", "AC23 8FD").mkString("\n")

    ("", addressLines, "UK") match {
      case AddressParser(addr) => verifyNamedAddress(expectedAddress, addr, "15 Holly Lane")
      case _ => fail("No Address Match made")
    }
  }

  test("Parser correctly uses the first line of the address as short name if the short name is only made up of spaces") {
    val addressLines = List("15 Holly Lane,", "A Town,", "A County,", "AC23 8FD.")
    val expectedAddress = List("15 Holly Lane", "A Town", "A County", "AC23 8FD").mkString("\n")

    ("   ", addressLines, "UK") match {
      case AddressParser(addr) => verifyNamedAddress(expectedAddress, addr, "15 Holly Lane")
      case _ => fail("No Address Match made")
    }
  }

  test("Parser correctly uses the first line of the address as short name if the short name is made up of spaces, tabs and new lines") {
    val addressLines = List("15 Holly Lane,", "A Town,", "A County,", "AC23 8FD.")
    val expectedAddress = List("15 Holly Lane", "A Town", "A County", "AC23 8FD").mkString("\n")

    (" \t  \n  ", addressLines, "UK") match {
      case AddressParser(addr) => verifyNamedAddress(expectedAddress, addr, "15 Holly Lane")
      case _ => fail("No Address Match made")
    }
  }

  test("Address with only a country code fails to generate address") {
    ("Short", Nil, "UK") match {
      case AddressParser(addr) => fail("No address text should not generate an address")
      case _ => //passed
    }
  }

  test("Address with only a single address line generates an Address") {
    ("Short", List("One Line"), "UK") match {
      case AddressParser(addr) => addr should be(Address("Short", "One Line", "United Kingdom"))
      case _ => fail("Expected Address not Generated")
    }
  }

  test("Address with empty address lines fails to generate address") {
    ("Short", List("", "  ", "\n", "\t"), "UK") match {
      case AddressParser(addr) => fail("No address text should not generate an address")
      case _ => //passed
    }
  }

  test("Address with out a country code fails to generate address") {
    ("Short", List("Address Text"), "") match {
      case AddressParser(addr) => fail("No country code should not generate an address")
      case _ => //passed
    }
  }

  test("Passing an empty list as the address text correctly returns None") {
    ("", Nil, "") match {
      case AddressParser(addr) => fail("No match expected for an empty list address text with empty short name and country")
      case _ => // passed
    }

    ("Short", Nil, "United Kingdom") match {
      case AddressParser(addr) => fail("No match expected for an empty list address text")
      case _ => // passed
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