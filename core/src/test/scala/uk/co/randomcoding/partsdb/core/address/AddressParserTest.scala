/**
 *
 */
package uk.co.randomcoding.partsdb.core.address

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import uk.co.randomcoding.partsdb.core.id.Identifier

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddressParserTest extends FunSuite with ShouldMatchers {
  test("Test can match a UK address string with lines separated by commas and new lines") {
    val addressString = "15 Holly Lane,\n" +
      "A Town,\n" +
      "A County,\n" +
      "AC23 8FD,\n" +
      "UK"

    val expectedAddress = List("15 Holly Lane", "A Town", "A County", "AC23 8FD", "UK").mkString("\n")
    addressString match {
      case AddressParser(addr) => addr should be(Address(Identifier(0), "15 Holly Lane", expectedAddress, "United Kingdom"))
      case _ => fail("No Address Match made")
    }
  }

  test("Test can match a UK address string with lines separated by both full stops and commas and new lines") {
    val addressString = "15 Holly Lane,\n" +
      "A Town,\n" +
      "A County,\n" +
      "AC23 8FD.\n" +
      "UK"

    val expectedAddress = List("15 Holly Lane", "A Town", "A County", "AC23 8FD", "UK").mkString("\n")
    addressString match {
      case AddressParser(addr) => addr should be(Address(Identifier(0), "15 Holly Lane", expectedAddress, "United Kingdom"))
      case _ => fail("No Address Match made")
    }
  }

  test("Test can match a UK address string with all address lines on the same line separated only by full stops and commas (no spaces)") {
    val addressString = """15 Holly Lane,A Town,A County,AC23 8FD.UK"""

    val expectedAddress = List("15 Holly Lane", "A Town", "A County", "AC23 8FD", "UK").mkString("\n")
    addressString match {
      case AddressParser(addr) => addr should be(Address(Identifier(0), "15 Holly Lane", expectedAddress, "United Kingdom"))
      case _ => fail("No Address Match made")
    }
  }

  test("Test can match a UK address string with all address lines on the same line separated only by full stops and commas with trailing spaces") {
    val addressString = """15 Holly Lane, A Town, A County, AC23 8FD. UK"""

    val expectedAddress = List("15 Holly Lane", "A Town", "A County", "AC23 8FD", "UK").mkString("\n")
    addressString match {
      case AddressParser(addr) => addr should be(Address(Identifier(0), "15 Holly Lane", expectedAddress, "United Kingdom"))
      case _ => fail("No Address Match made")
    }
  }

  test("Test can match a UK address without a UK country") {
    val addressString = """15 Holly Lane, A Town, A County, AC23 8FD."""

    val expectedAddress = List("15 Holly Lane", "A Town", "A County", "AC23 8FD").mkString("\n")
    addressString match {
      case AddressParser(addr) => addr should be(Address(Identifier(0), "15 Holly Lane", expectedAddress, "United Kingdom"))
      case _ => fail("No Address Match made")
    }
  }

  private val addressStringMatch = (addressString: String) => {
    val expectedAddress = List("15 Holly Lane", "A Town", "A County", "AC23 8FD", "UK").mkString("\n")
    addressString match {
      case AddressParser(addr) => addr should be(Address(Identifier(0), "15 Holly Lane", expectedAddress, "United Kingdom"))
      case _ => fail("No Address Match made")
    }
  }
}