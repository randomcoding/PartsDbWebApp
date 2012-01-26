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
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 * @author Jane Rowe
 *
 * @deprecated("Class under test is deprecated"
 */
class MongoDbNewIdentifierTest /*extends MongoDbTestBase with ShouldMatchers*/ {
  /*  override val dbName = "updateIdTest"

  private lazy val access = new MongoIdentifierAccess() {
    override val collection = mongo
  }

  // Address Tests
  test("Address with default id is corrently assigned new id") {
    val addr = Address(DefaultIdentifier, "Addr", "Address Text", "UK")
    access assignId addr should be(Address(Identifier(0), "Addr", "Address Text", "UK"))
  }

  test("Customer with default id is corrently assigned new id") {
    val cust = Customer(DefaultIdentifier, "Customer", Identifier(10), PaymentTerms(30), ContactDetails("Person", Some(List(Phone("+44123")))))
    access assignId cust should be(Customer(Identifier(0), "Customer", Identifier(10), PaymentTerms(30), ContactDetails("Person", Some(List(Phone("+44123"))))))
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

  // Part Tests with no options
  test("Part with default id is correctly assigned new id") {
    val part = Part(DefaultIdentifier, "Part1")
    access assignId part should be(Part(Identifier(0), "Part1"))
  }

  test("Sending multiple different parts with default ids result in different ids being assigned") {
    val part1 = Part(DefaultIdentifier, "Part1")
    val part2 = Part(DefaultIdentifier, "Part2")
    val part3 = Part(DefaultIdentifier, "Part3")
    val part4 = Part(DefaultIdentifier, "Part4")

    access assignId part1 should be(Part(Identifier(0), "Part1"))
    access assignId part2 should be(Part(Identifier(1), "Part2"))
    access assignId part3 should be(Part(Identifier(2), "Part3"))
    access assignId part4 should be(Part(Identifier(3), "Part4"))
  }

  test("Sending the same original part with a default id multiple times results in different ids being assigned") {
    val part1 = Part(DefaultIdentifier, "Part1")
    access assignId part1 should be(Part(Identifier(0), "Part1"))
    access assignId part1 should be(Part(Identifier(1), "Part1"))
    access assignId part1 should be(Part(Identifier(2), "Part1"))
    access assignId part1 should be(Part(Identifier(3), "Part1"))
  }

  test("Sending the same part multiple times results in only one change of id and subsequent calls return the same item reference") {
    val part1 = Part(DefaultIdentifier, "Part1")
    val part2 = access assignId part1
    val part3 = access assignId part2
    val part4 = access assignId part3
    val part5 = access assignId part4
    val cost1: Double = 0.1

    part2 should be(Part(Identifier(0), "Part1"))
    part3 should be(Part(Identifier(0), "Part1"))
    part4 should be(Part(Identifier(0), "Part1"))
    part5 should be(Part(Identifier(0), "Part1"))

    part2 should (be theSameInstanceAs part3 and
      be theSameInstanceAs part4 and
      be theSameInstanceAs part5)
  }

  // Part Tests with option
  //  test("Part with default id is correctly assigned new id") {
  //    val part = Part(DefaultIdentifier, "ModPartId", "SupplierPartId", "No Part", "00.00".toDouble, Some(Vehicle(Identifier(199), "SomeVehicle")))
  //    val cost: Double = 00.00
  //    access assignId part should be(Part(Identifier(0), "ModPartId", "SupplierPartId", "No Part", cost, Some(Vehicle(Identifier(199), "SomeVehicle"))))
  //  }
  //
  //  test("Sending multiple different parts with default ids result in different ids being assigned") {
  //    val part1 = Part(DefaultIdentifier, "ModPartId", "SupplierPartId", "Part1", 0.1, Some(Vehicle(Identifier(200), "Vehicle200")))
  //    val part2 = Part(DefaultIdentifier, "ModPartId", "SupplierPartId", "Part2", 0.2, Some(Vehicle(Identifier(200), "Vehicle200")))
  //    val part3 = Part(DefaultIdentifier, "ModPartId", "SupplierPartId", "Part3", 0.3, Some(Vehicle(Identifier(200), "Vehicle200")))
  //    val part4 = Part(DefaultIdentifier, "ModPartId", "SupplierPartId", "Part4", 0.4, Some(Vehicle(Identifier(200), "Vehicle200")))
  //
  //    val cost1: Double = 0.1
  //    val cost2: Double = 0.2
  //    val cost3: Double = 0.3
  //    val cost4: Double = 0.4
  //
  //    access assignId part1 should be(Part(Identifier(0), "ModPartId1", "SupplierPartId1", "Part1", cost1, Some(Vehicle(Identifier(200), "Vehicle200"))))
  //    access assignId part2 should be(Part(Identifier(1), "ModPartId2", "SupplierPartId2", "Part2", cost2, Some(Vehicle(Identifier(200), "Vehicle200"))))
  //    access assignId part3 should be(Part(Identifier(2), "ModPartId3", "SupplierPartId3", "Part3", cost3, Some(Vehicle(Identifier(200), "Vehicle200"))))
  //    access assignId part4 should be(Part(Identifier(3), "ModPartId4", "SupplierPartId4", "Part4", cost4, Some(Vehicle(Identifier(200), "Vehicle200"))))
  //  }
  //
  //  test("Sending the same original part with a default id multiple times results in different ids being assigned") {
  //    val cost1: Double = 00.1
  //    val part1 = Part(DefaultIdentifier, "ModPartId", "SupplierPartId", "Part1", "0.10".toDouble, Some(Vehicle(Identifier(200), "Vehicle200")))
  //    access assignId part1 should be(Part(Identifier(0), "ModPartId", "SupplierPartId", "Part1", cost1, Some(Vehicle(Identifier(200), "Vehicle200"))))
  //    access assignId part1 should be(Part(Identifier(1), "ModPartId", "SupplierPartId", "Part1", cost1, Some(Vehicle(Identifier(200), "Vehicle200"))))
  //    access assignId part1 should be(Part(Identifier(2), "ModPartId", "SupplierPartId", "Part1", cost1, Some(Vehicle(Identifier(200), "Vehicle200"))))
  //    access assignId part1 should be(Part(Identifier(3), "ModPartId", "SupplierPartId", "Part1", cost1, Some(Vehicle(Identifier(200), "Vehicle200"))))
  //  }
  //
  //  test("Sending the same part multiple times results in only one change of id and subsequent calls return the same item reference") {
  //    val part1 = Part(DefaultIdentifier, "ModPartId", "SupplierPartId", "Part1", 0.1, Some(Vehicle(Identifier(200), "Vehicle200")))
  //    val part2 = access assignId part1
  //    val part3 = access assignId part2
  //    val part4 = access assignId part3
  //    val part5 = access assignId part4
  //    val cost1: Double = 0.1
  //
  //    part2 should be(Part(Identifier(0), "ModPartId", "SupplierPartId", "Part1", cost1, Some(Vehicle(Identifier(200), "Vehicle200"))))
  //    part3 should be(Part(Identifier(0), "ModPartId", "SupplierPartId", "Part1", cost1, Some(Vehicle(Identifier(200), "Vehicle200"))))
  //    part4 should be(Part(Identifier(0), "ModPartId", "SupplierPartId", "Part1", cost1, Some(Vehicle(Identifier(200), "Vehicle200"))))
  //    part5 should be(Part(Identifier(0), "ModPartId", "SupplierPartId", "Part1", cost1, Some(Vehicle(Identifier(200), "Vehicle200"))))
  //
  //    part2 should (be theSameInstanceAs part3 and
  //      be theSameInstanceAs part4 and
  //      be theSameInstanceAs part5)
  //  }

  // Vehicle Tests
  test("Vehicle with default id is correctly assigned new id") {
    val vehicle = Vehicle(DefaultIdentifier, "SomeVehicle")
    access assignId vehicle should be(Vehicle(Identifier(0), "SomeVehicle"))
  }

  test("Sending multiple different vehicles with default ids result in different ids being assigned") {
    val vehicle1 = Vehicle(DefaultIdentifier, "Vehicle1")
    val vehicle2 = Vehicle(DefaultIdentifier, "Vehicle2")
    val vehicle3 = Vehicle(DefaultIdentifier, "Vehicle3")
    val vehicle4 = Vehicle(DefaultIdentifier, "Vehicle4")

    access assignId vehicle1 should be(Vehicle(Identifier(0), "Vehicle1"))
    access assignId vehicle2 should be(Vehicle(Identifier(1), "Vehicle2"))
    access assignId vehicle3 should be(Vehicle(Identifier(2), "Vehicle3"))
    access assignId vehicle4 should be(Vehicle(Identifier(3), "Vehicle4"))
  }

  test("Sending the same original vehicle with a default id multiple times results in different ids being assigned") {
    val vehicle1 = Vehicle(DefaultIdentifier, "Vehicle1")
    access assignId vehicle1 should be(Vehicle(Identifier(0), "Vehicle1"))
    access assignId vehicle1 should be(Vehicle(Identifier(1), "Vehicle1"))
    access assignId vehicle1 should be(Vehicle(Identifier(2), "Vehicle1"))
    access assignId vehicle1 should be(Vehicle(Identifier(3), "Vehicle1"))
  }

  test("Sending the same vehicle multiple times results in only one change of id and subsequent calls return the same item reference") {
    val vehicle1 = Vehicle(DefaultIdentifier, "Vehicle200")
    val vehicle2 = access assignId vehicle1
    val vehicle3 = access assignId vehicle2
    val vehicle4 = access assignId vehicle3
    val vehicle5 = access assignId vehicle4

    vehicle2 should be(Vehicle(Identifier(0), "Vehicle200"))
    vehicle3 should be(Vehicle(Identifier(0), "Vehicle200"))
    vehicle4 should be(Vehicle(Identifier(0), "Vehicle200"))
    vehicle5 should be(Vehicle(Identifier(0), "Vehicle200"))

    vehicle2 should (be theSameInstanceAs vehicle3 and
      be theSameInstanceAs vehicle4 and
      be theSameInstanceAs vehicle5)
  }*/

}