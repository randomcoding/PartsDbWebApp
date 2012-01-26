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
 *
 * @deprecated("Changing Access API")
 */
class DbAccessAddCustomerTest extends MongoDbTestBase {
  /*override val dbName = "DbAccessAddCustomerTest"
  lazy val databaseName = dbName
  lazy val collName = collectionName

  private lazy val access = new DbAccess {
    override val dbName = databaseName
    override val collectionName = collName
  }*/

  test("Add a customer with new addresses adds all elements to the database") {
    fail("DB Access has changed")
    /*val bAddr = Address(DefaultIdentifier, "Billing", "Billing Address", "UK")

    val newCust = access.addNewCustomer("Customer", bAddr, PaymentTerms(30), ContactDetails("Customer", Some(List(Phone("+44321456789")))))

    newCust should be(Some(Customer(Identifier(1), "Customer", Identifier(0), PaymentTerms(30), ContactDetails("Customer", Some(List(Phone("+44321456789")))))))

    access.getAll[Address]("addressId") should be(List(Address(Identifier(0), "Billing", "Billing Address", "UK")))

    access.getAll[Customer]("customerId") should be(List(newCust.get))*/
  }

  test("Add a customer with billing address already in database (using same addressId) does not add a duplicate address") {
    fail("DB Access has changed")
    /*val bAddr = Address(Identifier(2345), "Billing", "Billing Address", "UK")

    val newCust = access.addNewCustomer("Customer", bAddr, PaymentTerms(30), ContactDetails("Customer", Some(List(Phone("+44321456789")))))

    newCust should be(Some(Customer(Identifier(0), "Customer", Identifier(2345), PaymentTerms(30), ContactDetails("Customer", Some(List(Phone("+44321456789")))))))

    access.getAll[Address]("addressId") should be(List(bAddr))

    access.getAll[Customer]("customerId") should be(List(newCust.get))*/
  }

  // This will fail until issue 13 is fixed
  ignore("Simulate add customer which duplicates a billing address already in database but uses default addressId does not add a duplicate address") {
    /*val bAddr1 = Address(Identifier(15), "Billing", "Billing Address", "UK")
    (access.add(bAddr1)) should be(true)
    val bAddr2 = Address(DefaultIdentifier, "Billing", "Billing Address", "UK")

    val newCust = access.addNewCustomer("Customer", bAddr2, PaymentTerms(30), ContactDetails("Customer", Some(List(Phone("+44321456789")))))
    newCust should be(Some(Customer(Identifier(1), "Customer", Identifier(15), PaymentTerms(30), ContactDetails("Customer", Some(List(Phone("+44321456789")))))))

    access.getAll[Address]("addressId") should be(List(bAddr1))

    access.getAll[Customer]("customerId") should be(List(newCust.get))*/
  }

}