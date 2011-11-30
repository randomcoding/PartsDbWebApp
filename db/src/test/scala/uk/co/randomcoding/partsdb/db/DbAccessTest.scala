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
class DbAccessTest extends MongoDbTestBase {
  override val dbName = "DbAccessAddAdddressTest"
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

  test("Add a customer with existing billing address (with same addressId) does not add a duplicate address") {
    val bAddr = Address(Identifier(2345), "Billing", "Billing Address", "UK")
    val dAddr = Address(Identifier(3456), "Delivery", "Delivery Address", "UK")

    val newCust = access.addNewCustomer("Customer", bAddr, dAddr, PaymentTerms(30), ContactDetails("Customer", Some(List(Phone("+44321456789")))))

    newCust should be(Customer(Identifier(0), "Customer", Identifier(2345), Set(Identifier(3456)), PaymentTerms(30), ContactDetails("Customer", Some(List(Phone("+44321456789"))))))

    access.getAll[Address]("addressId") should (have size (2) and
      contain(bAddr) and
      contain(dAddr))

    access.getAll[Customer]("customerId") should be(List(newCust))
  }

  test("Add a customer with existing delivery address (with same addressId) does not add a duplicate address") {
    pending
  }

  test("Add a customer with existing billing address (with default addressId) does not add a duplicate address") {
    pending
  }

  test("Add a customer with existing delivery address (with default addressId) does not add a duplicate address") {
    pending
  }

}