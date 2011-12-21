/**
 *
 */
package uk.co.randomcoding.partsdb.db.search

import org.scalatest.OneInstancePerTest

import uk.co.randomcoding.partsdb.core.contact.{ Phone, ContactDetails, Email }
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.id.Identifier
import uk.co.randomcoding.partsdb.core.terms.PaymentTerms
import uk.co.randomcoding.partsdb.db.mongo.{ MongoUpdateAccess, MongoDbTestBase }
import MongoSearchTerm._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class MongoCustomerSearchProviderTest extends MongoDbTestBase with OneInstancePerTest {
  override val dbName = "CustomerSearchTest"

  lazy val access = new MongoUpdateAccess {
    override val collection = mongo
  }

  lazy val provider = CustomerSearchProvider(mongo)

  test("Search for a customer in empty database returns no results") {
    provider find StringSearchTerm("customerName", "A Customer") should be('empty)
  }

  test("Search for single customer in database with single entry returns correct result") {
    val cust1 = Customer(Identifier(0), "Customer", Identifier(123), PaymentTerms(30), ContactDetails("Dave", Some(List(Phone("456789", false)))))
    access add cust1 should be(true)
    provider find StringSearchTerm("customerName", "Customer") should be(List(cust1))

    provider find StringSearchTerm("contactDetails.contactName", "Dave") should be(List(cust1))

    provider find IntegerSearchTerm("terms.days", 30) should be(List(cust1))

    provider find Set(IntegerSearchTerm("terms.days", 30), MongoSearchTerm("contactDetails.contactName", "Dave")) should be(List(cust1))
  }

  test("Search that does not match any records in database returns no results") {
    val cust1 = Customer(Identifier(0), "Customer", Identifier(123), PaymentTerms(30), ContactDetails("Dave", Some(List(Phone("456789", false)))))
    val cust2 = Customer(Identifier(1), "AN Customer", Identifier(125), PaymentTerms(30), ContactDetails("Sue", emailAddresses = Some(List(Email("hi@here.net")))))
    val cust3 = Customer(Identifier(2), "AN Other Customer", Identifier(123), PaymentTerms(30), ContactDetails("David", Some(List(Phone("456789", false)))))
    access add cust1 should be(true)
    access add cust2 should be(true)
    access add cust3 should be(true)

    provider find MongoSearchTerm("customerName", "A Customer") should be('empty)
    provider find Set(MongoSearchTerm("billingAddress.id", 124), MongoSearchTerm("customerName", "Customer")) should be('empty)
  }

  test("Search tham matches multiple records in database returns correct results") {
    val cust1 = Customer(Identifier(0), "Customer", Identifier(123), PaymentTerms(30), ContactDetails("Dave", Some(List(Phone("456789", false)))))
    val cust2 = Customer(Identifier(1), "AN Customer", Identifier(125), PaymentTerms(30), ContactDetails("Sue", emailAddresses = Some(List(Email("hi@here.net")))))
    val cust3 = Customer(Identifier(2), "AN Other Customer", Identifier(123), PaymentTerms(30), ContactDetails("David", Some(List(Phone("456789", false)))))
    access add cust1 should be(true)
    access add cust2 should be(true)
    access add cust3 should be(true)

    provider find MongoSearchTerm("billingAddress.id", 125) should be(List(cust2))
    provider find Set(MongoSearchTerm("billingAddress.id", 123), MongoSearchTerm("contactDetails.phoneNumbers.phoneNumber", "456789")) should (have size (2) and
      contain(cust1) and
      contain(cust3))
  }

  test("Search Using a regex to do substring matches") {
    val cust1 = Customer(Identifier(0), "Customer", Identifier(123), PaymentTerms(30), ContactDetails("Dave", Some(List(Phone("456789", false)))))
    val cust2 = Customer(Identifier(1), "AN Customer", Identifier(125), PaymentTerms(30), ContactDetails("Sue", emailAddresses = Some(List(Email("hi@here.net")))))
    val cust3 = Customer(Identifier(2), "AN Other Customer", Identifier(123), PaymentTerms(30), ContactDetails("David", Some(List(Phone("456789", false)))))
    access add cust1 should be(true)
    access add cust2 should be(true)
    access add cust3 should be(true)

    provider find MongoSearchTerm("customerName", ".*AN.*".r) should (have size (2) and
      contain(cust2) and
      contain(cust3))
  }
}