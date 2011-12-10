/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.id.{ Identifier, DefaultIdentifier }
import uk.co.randomcoding.partsdb.core.terms.PaymentTerms
import uk.co.randomcoding.partsdb.core.contact.{ ContactDetails, Phone }
import uk.co.randomcoding.partsdb.core.part.Part

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 * @author Jane Rowe
 */
class MongoDbNewIdentifierTest extends MongoDbTestBase with ShouldMatchers {
  override val dbName = "updateIdTest"

  private lazy val access = new MongoIdentifierAccess() {
    override val collection = mongo
  }

  // Address Tests
  test("Address with default id is corrently assigned new id") {
    val addr = Address(DefaultIdentifier, "Addr", "Address Text", "UK")
    access assignId addr should be(Address(Identifier(0), "Addr", "Address Text", "UK"))
  }

  test("Customer with default id is corrently assigned new id") {
    val cust = Customer(DefaultIdentifier, "Customer", Identifier(10), Set(Identifier(20)), PaymentTerms(30), ContactDetails("Person", Some(List(Phone("+44123")))))
    access assignId cust should be(Customer(Identifier(0), "Customer", Identifier(10), Set(Identifier(20)), PaymentTerms(30), ContactDetails("Person", Some(List(Phone("+44123"))))))
  }

  test("Sending multiple different items with default ids result in different ids being assigned") {
    val addr1 = Address(DefaultIdentifier, "Addr1", "Address 1", "UK")
    val addr2 = Address(DefaultIdentifier, "Addr2", "Address 2", "UK")
    val addr3 = Address(DefaultIdentifier, "Addr3", "Address 3", "UK")
    val addr4 = Address(DefaultIdentifier, "Addr4", "Address 4", "UK")

    access assignId addr1 should be(Address(Identifier(0), "Addr1", "Address 1", "UK"))
    access assignId addr2 should be(Address(Identifier(1), "Addr2", "Address 2", "UK"))
    access assignId addr3 should be(Address(Identifier(2), "Addr3", "Address 3", "UK"))
    access assignId addr4 should be(Address(Identifier(3), "Addr4", "Address 4", "UK"))
  }

  test("Sending the same original item with a default id multiple times results in different ids being assigned") {
    val addr1 = Address(DefaultIdentifier, "Addr1", "Address 1", "UK")
    access assignId addr1 should be(Address(Identifier(0), "Addr1", "Address 1", "UK"))
    access assignId addr1 should be(Address(Identifier(1), "Addr1", "Address 1", "UK"))
    access assignId addr1 should be(Address(Identifier(2), "Addr1", "Address 1", "UK"))
    access assignId addr1 should be(Address(Identifier(3), "Addr1", "Address 1", "UK"))
  }

  test("Sending the same item multiple times results in only one change of id and subsequent cals return the same item reference") {
    val addr1 = Address(DefaultIdentifier, "Addr1", "Address 1", "UK")
    val addr2 = access assignId addr1
    val addr3 = access assignId addr2
    val addr4 = access assignId addr2
    val addr5 = access assignId addr3

    addr2 should be(Address(Identifier(0), "Addr1", "Address 1", "UK"))
    addr3 should be(Address(Identifier(0), "Addr1", "Address 1", "UK"))
    addr4 should be(Address(Identifier(0), "Addr1", "Address 1", "UK"))
    addr5 should be(Address(Identifier(0), "Addr1", "Address 1", "UK"))

    addr2 should (be theSameInstanceAs addr3 and
      be theSameInstanceAs addr4 and
      be theSameInstanceAs addr5)
  }

  // Part Tests
  test("Part with default id is corrently assigned new id") {
    val part = Part(DefaultIdentifier, "No Part", "00.00".toDouble)
    val cost: Double = 00.00
    access assignId part should be(Part(Identifier(0), "No Part", cost))
  }

  test("Sending multiple different parts with default ids result in different ids being assigned") {
    val part1 = Part(DefaultIdentifier, "Part1", "0.10".toDouble)
    val part2 = Part(DefaultIdentifier, "Part2", "0.20".toDouble)
    val part3 = Part(DefaultIdentifier, "Part3", "0.30".toDouble)
    val part4 = Part(DefaultIdentifier, "Part4", "0.40".toDouble)

    val cost1: Double = 00.1
    val cost2: Double = 00.2
    val cost3: Double = 00.3
    val cost4: Double = 00.4

    access assignId part1 should be(Part(Identifier(0), "Part1", cost1))
    access assignId part2 should be(Part(Identifier(1), "Part2", cost2))
    access assignId part3 should be(Part(Identifier(2), "Part3", cost3))
    access assignId part4 should be(Part(Identifier(3), "Part4", cost4))
  }

  test("Sending the same original part with a default id multiple times results in different ids being assigned") {
    val cost1: Double = 00.1
    val part1 = Part(DefaultIdentifier, "Part1", "0.10".toDouble)
    access assignId part1 should be(Part(Identifier(0), "Part1", cost1))
    access assignId part1 should be(Part(Identifier(1), "Part1", cost1))
    access assignId part1 should be(Part(Identifier(2), "Part1", cost1))
    access assignId part1 should be(Part(Identifier(3), "Part1", cost1))
  }

  test("Sending the same part multiple times results in only one change of id and subsequent cals return the same item reference") {
    val part1 = Part(DefaultIdentifier, "Part1", "0.10".toDouble)
    val part2 = access assignId part1
    val part3 = access assignId part2
    val part4 = access assignId part3
    val part5 = access assignId part4
    val cost1: Double = 00.1

    part2 should be(Part(Identifier(0), "Part1", cost1))
    part3 should be(Part(Identifier(0), "Part1", cost1))
    part4 should be(Part(Identifier(0), "Part1", cost1))
    part5 should be(Part(Identifier(0), "Part1", cost1))

    part2 should (be theSameInstanceAs part3 and
      be theSameInstanceAs part4 and
      be theSameInstanceAs part5)
  }

}