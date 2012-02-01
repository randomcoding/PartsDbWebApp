/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import com.foursquare.rogue.Rogue._
import org.bson.types.ObjectId

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class CustomerRecordTest extends MongoDbTestBase {
  import uk.co.randomcoding.partsdb.core.address.Address
  import uk.co.randomcoding.partsdb.core.terms.PaymentTerms
  import uk.co.randomcoding.partsdb.core.contact.ContactDetails
  import uk.co.randomcoding.partsdb.core.contact.ContactDetails._
  import uk.co.randomcoding.partsdb.core.customer.Customer
  import uk.co.randomcoding.partsdb.core.customer.Customer._

  override val dbName = "CustomerRecordTest"

  test("Adding a single customer works ok") {
    val addr = Address.createRecord.shortName("Addr1").addressText("Address").country("UK")
    val cust = add("cust1", addr, 30, ContactDetails("Dave"))

    val expectedCustomer = createRecord.customerName("cust1").businessAddress(addr.id.get).terms(30).contactDetails(ContactDetails("Dave"))

    cust should be(Some(expectedCustomer))

    findNamed("cust1") should be(List(expectedCustomer))

    findById(cust.get.id.get) should be(Some(expectedCustomer))
  }

  test("Adding the same customer more than once does not result in a duplication, or a change of underlying id") {
    val addr = Address.createRecord.shortName("Addr1").addressText("Address").country("UK")
    val cust = add("cust1", addr, 30, ContactDetails("Dave"))
    cust should be('defined)
    add("cust1", addr, 30, ContactDetails("Dave")) should be('empty)

    val expectedCustomer = createRecord.customerName("cust1").businessAddress(addr.id.get).terms(30).contactDetails(ContactDetails("Dave"))

    (Customer where (_.id exists true) fetch) should be(List(expectedCustomer))

    findNamed("cust1")(0).id.get should be(cust.get.id.get)
  }

  test("Removing a customer") {
    val addr = Address.createRecord.shortName("Addr1").addressText("Address").country("UK")
    val cust1 = add("cust1", addr, 30, ContactDetails("Dave")).get
    val cust2 = add("cust2", addr, 45, ContactDetails("Sally")).get

    val custId = cust1.id.get
    remove(custId) should be(List(true))

    findNamed("cust1") should be('empty)
    findById(custId) should be('empty)

    (Customer where (_.id exists true) fetch) should be(List(cust2))
  }

  test("Removing a customer from an empty database") {
    remove(new ObjectId) should be(Nil)
  }

  test("Find a customer that is present in the database") {
    val addr = Address.createRecord.shortName("Addr1").addressText("Address").country("UK")
    val cust1 = add("cust1", addr, 30, ContactDetails("Dave")).get

    findNamed("cust1") should be(List(cust1))

    findById(cust1.id.get) should be(Some(cust1))
  }

  test("Find a customer that is not present in the database") {
    val addr = Address.createRecord.shortName("Addr1").addressText("Address").country("UK")
    val cust1 = add("cust1", addr, 30, ContactDetails("Dave")).get

    findNamed("Customer2") should be(Nil)

    findById(new ObjectId("4f2871f4231823ddb82a080c")) should be('empty)
  }

  test("Modify a customer") {
    val addr = Address.createRecord.shortName("Addr1").addressText("Address").country("UK")
    val addr2 = Address.createRecord.shortName("Addr2").addressText("Address Again").country("USA")
    val cust1 = add("cust1", addr, 30, ContactDetails("Dave")).get

    modify(cust1.id.get, "Customer 1-1", addr2, 45, ContactDetails("Sally"))

    findNamed("cust1") should be(Nil)
    findNamed("Customer 1-1") should be(List(createRecord.customerName("Customer 1-1").businessAddress(addr2.id.get).terms(45).contactDetails(ContactDetails("Sally"))))
  }

  test("Modfy a customer does not modify its object id") {
    val addr = Address.createRecord.shortName("Addr1").addressText("Address").country("UK")
    val addr2 = Address.createRecord.shortName("Addr2").addressText("Address Again").country("USA")
    val cust1 = add("cust1", addr, 30, ContactDetails("Dave")).get
    val origId = cust1.id.get
    modify(cust1.id.get, "Customer 1-1", addr2, 45, ContactDetails("Sally"))

    findNamed("cust1") should be(Nil)
    findById(origId) should be(List(createRecord.customerName("Customer 1-1").businessAddress(addr2.id.get).terms(45).contactDetails(ContactDetails("Sally"))))
  }
}