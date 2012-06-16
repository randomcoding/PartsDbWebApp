/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import org.bson.types.ObjectId

import com.foursquare.rogue.Rogue._

import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.contact.ContactDetails
import uk.co.randomcoding.partsdb.core.customer.Customer._
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.util.MongoHelpers._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class CustomerRecordTest extends MongoDbTestBase {

  override val dbName = "CustomerRecordTest"
  private[this] val contactDave = ContactDetails.create("Dave", "4321", "", "", "", true)
  private[this] val contactSally = ContactDetails.create("Sally", "9876", "", "", "", true)
  private[this] val addr = Address("Addr1", "Address", "United Kingdom")
  private[this] val addr2 = Address("Addr2", "Address 2", "United States")

  test("Equality and HashCode") {
    val cust1 = create("cust1", addr, 30, contactDave)
    val cust2 = create("cust1", addr, 30, contactDave)
    val cust3 = create("cust1", addr, 30, contactDave)

    cust1 should (be(cust2) and be(cust3))
    cust2 should (be(cust1) and be(cust3))
    cust3 should (be(cust1) and be(cust2))

    cust1.hashCode should (be(cust2.hashCode) and be(cust3.hashCode))

    val cust4 = create("Cust 2", addr, 30, contactDave)
    cust4 should not equal (cust1)
    cust1 should not equal (cust4)
    cust4.hashCode should not be (cust1.hashCode)
  }

  test("Adding a single customer works ok") {
    val cust = add("cust1", addr, 30, contactDave)

    val expectedCustomer = create("cust1", addr, 30, contactDave)

    cust should be(Some(expectedCustomer))

    findNamed("cust1") should be(List(expectedCustomer))

    findById(cust.get.id.get) should be(Some(expectedCustomer))
  }

  test("Adding the same customer more than once does not result in a duplication, or a change of underlying id") {
    val cust = add("cust1", addr, 30, contactDave)
    cust should be('defined)
    add("cust1", addr, 30, contactDave) should be(cust)

    val expectedCustomer = create("cust1", addr, 30, contactDave)

    (Customer fetch) should be(List(expectedCustomer))

    findNamed("cust1")(0).id.get should be(cust.get.id.get)
  }

  test("Removing a customer") {
    val cust1 = add("cust1", addr, 30, contactDave).get
    val cust2 = add("cust2", addr, 45, contactSally).get

    val custId = cust1.id.get
    remove(custId) should be(List(true))

    findNamed("cust1") should be('empty)
    findById(custId) should be('empty)

    (Customer fetch) should be(List(cust2))
  }

  test("Removing a customer from an empty database") {
    remove(new ObjectId) should be(Nil)
  }

  test("Find a customer that is present in the database") {
    val cust1 = add("cust1", addr, 30, contactDave).get

    findNamed("cust1") should be(List(cust1))

    findById(cust1.id.get) should be(Some(cust1))
  }

  test("Find a customer that is not present in the database") {
    val cust1 = add("cust1", addr, 30, contactDave).get

    findNamed("Customer2") should be(Nil)

    findById("4f2871f4231823ddb82a080c") should be('empty)
  }

  test("Modify a customer") {
    val cust1 = add("cust1", addr, 30, contactDave).get

    modify(cust1.id.get, "Customer 1-1", addr2, 45, List(contactSally))

    findNamed("cust1") should be(Nil)
    findNamed("Customer 1-1") should be(List(create("Customer 1-1", addr2, 45, contactSally)))
  }

  test("Modfy a customer does not modify its object id") {
    val cust1 = add("cust1", addr, 30, contactDave).get
    val origId = cust1.id.get
    modify(cust1.id.get, "Customer 1-1", addr2, 45, List(contactSally))

    findNamed("cust1") should be(Nil)
    findById(origId) should be(Some(create("Customer 1-1", addr2, 45, contactSally)))
    (Customer where (_.id eqs origId) fetch) should be(List(create("Customer 1-1", addr2, 45, contactSally)))
  }

  test("Adding A Customer does not add the address to the database") {
    val cust1 = add("cust1", addr, 30, contactDave)
    cust1 should be('defined)

    Address.findById(addr.id.get) should be(None)
    Address.findNamed("Addr1") should be(Nil)
  }

  test("Find Matching correctly finds a record with the same Object Id") {
    val cust1 = add("cust1", addr, 30, contactDave).get
    val otherCust = create("Another Customer", addr, 30, contactDave).id(cust1.id.get)

    findMatching(otherCust) should be(Some(cust1))
  }

  test("Find Matching correctly finds a record with the same Customer Name") {
    val cust1 = add("cust1", addr, 30, contactDave).get
    val otherCust = create("cust1", addr2, 45, contactSally)

    findMatching(otherCust) should be(Some(cust1))
  }

  test("Modified contact details of a customer updates the correct instance of the contact details and does not duplicate it") {
    val cust1 = add("cust1", addr, 30, contactDave).get
    val contactsModified = ContactDetails.create("Dave", "4321", "321", "", "", true)
    Customer.modify(cust1.id.get, "cust1", addr, 30, List(contactsModified))

    (Customer where (_.id eqs cust1.id.get) fetch) should be(List(Customer.create("cust1", addr, 30, contactsModified).id(cust1.id.get)))
    Customer.findById(cust1.id.get).get.contactDetails.get should be(List(contactsModified))
  }
}