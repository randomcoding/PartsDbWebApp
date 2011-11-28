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

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class MongoDbNewIdentifierTest extends MongoDbTestBase with ShouldMatchers {
  override val dbName = "updateIdTest"

  private lazy val access = new MongoIdentifierAccess() {
    override val collection = mongo
  }

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

}