/**
 *
 */
package uk.co.randomcoding.partsdb.db.search

import org.scalatest.OneInstancePerTest

import uk.co.randomcoding.partsdb.core.contact.{ Phone, ContactDetails }
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.id.Identifier
import uk.co.randomcoding.partsdb.core.terms.PaymentTerms
import uk.co.randomcoding.partsdb.db.mongo.{ MongoUpdateAccess, MongoDbTestBase }

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
  }

  test("Search that does not match any records in database returns no results") {
    pending
  }

  test("Search tham matches multiple records in database returns correct results") {
    pending
  }

}