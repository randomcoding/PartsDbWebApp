/**
 *
 */
package uk.co.randomcoding.partsdb.db

import uk.co.randomcoding.partsdb.db.mongo.MongoDbTestBase
import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.id.DefaultIdentifier
import uk.co.randomcoding.partsdb.core.id.Identifier
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.terms.PaymentTerms
import uk.co.randomcoding.partsdb.core.contact.{ ContactDetails, Phone }

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class DbAccessAddCustomerTest extends MongoDbTestBase {
  override val dbName = "DbAccessAddCustomerTest"
  lazy val databaseName = dbName
  lazy val collName = collectionName

  private lazy val access = new DbAccess {
    override val dbName = databaseName
    override val collectionName = collName
  }

  test("Add a customer with new addresses adds all elements to the database") {
    val bAddr = Address(DefaultIdentifier, "Billing", "Billing Address", "UK")
    val dAddr = Address(DefaultIdentifier, "Delivery", "Delivery Address", "UK")

    val newCust = access.addNewCustomer("Customer", bAddr, dAddr, PaymentTerms(30), ContactDetails("Customer", Some(List(Phone("+44321456789")))))

    newCust should be(Customer(Identifier(2), "Customer", Identifier(0), Set(Identifier(1)), PaymentTerms(30), ContactDetails("Customer", Some(List(Phone("+44321456789"))))))

    access.getAll[Address]("addressId") should (have size (2) and
      contain(Address(Identifier(0), "Billing", "Billing Address", "UK")) and
      contain(Address(Identifier(1), "Delivery", "Delivery Address", "UK")))

    access.getAll[Customer]("customerId") should be(List(newCust))
  }

  test("Add a customer with billing address already in database (using same addressId) does not add a duplicate address") {
    val bAddr = Address(Identifier(2345), "Billing", "Billing Address", "UK")
    val dAddr = Address(DefaultIdentifier, "Delivery", "Delivery Address", "UK")

    val newCust = access.addNewCustomer("Customer", bAddr, dAddr, PaymentTerms(30), ContactDetails("Customer", Some(List(Phone("+44321456789")))))

    newCust should be(Customer(Identifier(1), "Customer", Identifier(2345), Set(Identifier(0)), PaymentTerms(30), ContactDetails("Customer", Some(List(Phone("+44321456789"))))))

    access.getAll[Address]("addressId") should (have size (2) and
      contain(bAddr) and
      contain(Address(Identifier(0), "Delivery", "Delivery Address", "UK")))

    access.getAll[Customer]("customerId") should be(List(newCust))
  }

  test("Add a customer with delivery address already in database (using same addressId) does not add a duplicate address") {
    val bAddr = Address(DefaultIdentifier, "Billing", "Billing Address", "UK")
    val dAddr = Address(Identifier(3456), "Delivery", "Delivery Address", "UK")

    access.add(dAddr) should be(true)

    val newCust = access.addNewCustomer("Customer", bAddr, dAddr, PaymentTerms(30), ContactDetails("Customer", Some(List(Phone("+44321456789")))))
    newCust should be(Customer(Identifier(1), "Customer", Identifier(0), Set(Identifier(3456)), PaymentTerms(30), ContactDetails("Customer", Some(List(Phone("+44321456789"))))))

    access.getAll[Address]("addressId") should (contain(Address(Identifier(0), "Billing", "Billing Address", "UK")) and
      contain(dAddr) and
      have size (2))

    access.getAll[Customer]("customerId") should be(List(newCust))
  }

  test("Add a customer with delivery and billing addresses already in database (using same addressId) does not add a duplicate addresses") {
    val bAddr = Address(Identifier(2345), "Billing", "Billing Address", "UK")
    val dAddr = Address(Identifier(3456), "Delivery", "Delivery Address", "UK")
    access.add(bAddr) should be(true)
    access.add(dAddr) should be(true)

    val newCust = access.addNewCustomer("Customer", bAddr, dAddr, PaymentTerms(30), ContactDetails("Customer", Some(List(Phone("+44321456789")))))
    newCust should be(Customer(Identifier(0), "Customer", Identifier(2345), Set(Identifier(3456)), PaymentTerms(30), ContactDetails("Customer", Some(List(Phone("+44321456789"))))))

    access.getAll[Address]("addressId") should (contain(bAddr) and
      contain(dAddr) and
      have size (2))

    access.getAll[Customer]("customerId") should be(List(newCust))
  }

  // This will fail until issue 13 is fixed
  test("Simulate add customer which duplicates a billing address already in database but uses default addressId does not add a duplicate address") {
    val bAddr1 = Address(Identifier(15), "Billing", "Billing Address", "UK")
    (access.add(bAddr1)) should be(true)
    val bAddr2 = Address(DefaultIdentifier, "Billing", "Billing Address", "UK")
    val dAddr = Address(DefaultIdentifier, "Delivery", "Delivery Address", "UK")

    val newCust = access.addNewCustomer("Customer", bAddr2, dAddr, PaymentTerms(30), ContactDetails("Customer", Some(List(Phone("+44321456789")))))
    newCust should be(Customer(Identifier(1), "Customer", Identifier(15), Set(Identifier(0)), PaymentTerms(30), ContactDetails("Customer", Some(List(Phone("+44321456789"))))))

    access.getAll[Address]("addressId") should (contain(bAddr1) and
      contain(dAddr) and
      have size (2))

    access.getAll[Customer]("customerId") should be(List(newCust))
  }

  // This will fail until issue 13 is fixed
  test("Simulate add customer which duplicates a delivery address already in database but uses default addressId does not add a duplicate address") {
    val dAddr1 = Address(Identifier(150), "Delivery", "Delivery Address", "UK")
    (access.add(dAddr1)) should be(true)
    val bAddr = Address(DefaultIdentifier, "Billing", "Billing Address", "UK")
    val dAddr2 = Address(DefaultIdentifier, "Delivery", "Delivery Address", "UK")

    val newCust = access.addNewCustomer("Customer", bAddr, dAddr2, PaymentTerms(30), ContactDetails("Customer", Some(List(Phone("+44321456789")))))
    newCust should be(Customer(Identifier(1), "Customer", Identifier(0), Set(Identifier(150)), PaymentTerms(30), ContactDetails("Customer", Some(List(Phone("+44321456789"))))))

    access.getAll[Address]("addressId") should (contain(bAddr) and
      contain(dAddr1) and
      have size (2))

    access.getAll[Customer]("customerId") should be(List(newCust))
  }

  // This will fail until issue 13 is fixed
  test("Simulate add customer which duplicates billing and delivery addresses already in database but using default addressId does not add duplicate addresses") {
    val bAddr1 = Address(Identifier(15), "Billing", "Billing Address", "UK")
    (access.add(bAddr1)) should be(true)
    val dAddr1 = Address(Identifier(150), "Delivery", "Delivery Address", "UK")
    (access.add(dAddr1)) should be(true)
    val bAddr2 = Address(DefaultIdentifier, "Billing", "Billing Address", "UK")
    val dAddr2 = Address(DefaultIdentifier, "Delivery", "Delivery Address", "UK")

    val newCust = access.addNewCustomer("Customer", bAddr2, dAddr2, PaymentTerms(30), ContactDetails("Customer", Some(List(Phone("+44321456789")))))
    newCust should be(Customer(Identifier(0), "Customer", Identifier(15), Set(Identifier(150)), PaymentTerms(30), ContactDetails("Customer", Some(List(Phone("+44321456789"))))))

    access.getAll[Address]("addressId") should (contain(bAddr1) and
      contain(dAddr1) and
      have size (2))

    access.getAll[Customer]("customerId") should be(List(newCust))
  }
}