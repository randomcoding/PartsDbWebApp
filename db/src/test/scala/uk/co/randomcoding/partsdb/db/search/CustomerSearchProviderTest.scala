/**
 *
 */
package uk.co.randomcoding.partsdb.db.search

import org.scalatest.OneInstancePerTest
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.db.mongo.MongoDbTestBase
import CustomerSearchProvider._
import uk.co.randomcoding.partsdb.core.contact.ContactDetails

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class CustomerSearchProviderTest extends MongoDbTestBase {
  override val dbName = "CustomerSearchProviderTest"

  val daveAddress = Address.create("Dave Address", "12 The Road, The Town, A County. GH7 6TF", "UK")
  val sallyAddress = Address.create("Sally Address", "15 The Road, The Other Town, A Different County. DC7 5HG", "UK")

  val contactDave = ContactDetails.create("Dave", "01234 567890", "07890 123456", "dave@email.com", true)
  val contactSally = ContactDetails.create("Sally", "01987 654321", "07654 321098", "sally@snailmail.com", true)

  val customerDave = Customer.create("Dave's Places", daveAddress, 30, contactDave)
  val customerSally = Customer.create("Sally's Scouting", sallyAddress, 30, contactSally)

  override def beforeEach(): Unit = {
    super.beforeEach()

    Seq(daveAddress, sallyAddress, contactDave, contactSally) foreach (_ save)
  }

  test("Search for a customer in empty database returns no results") {
    findMatching(customerName = "name") should be('empty)
    findMatching(addressContains = "town") should be('empty)
    findMatching(contactName = "name") should be('empty)
    findMatching(contactPhoneNumber = "01234") should be('empty)
    findMatching(contactMobileNumber = "01234") should be('empty)
    findMatching(contactEmail = "em@ai.l") should be('empty)
  }

  test("Search for customer in database by single query term returns correct results") {
    Customer.add(customerDave)
    Customer.add(customerSally)
    findMatching(customerName = "place") should be(List(customerDave))
    findMatching(customerName = "scout") should be(List(customerSally))
    findMatching(customerName = "s S") should be(List(customerSally))

    findMatching(customerName = "Alan") should be('empty)

    findMatching(addressContains = "The Town") should be(List(customerDave))
    findMatching(contactName = "sally") should be(List(customerSally))
    findMatching(contactPhoneNumber = "01987") should be(List(customerSally))
    findMatching(contactMobileNumber = "12345") should be(List(customerDave))
    findMatching(contactEmail = "dave@email") should be(List(customerDave))
  }

  test("Search using multiple terms return expected results") {
    Customer.add(customerDave)
    Customer.add(customerSally)

    findMatching(addressContains = "The Road", contactEmail = "snailmail") should be(List(customerSally))
    findMatching(customerName = "sally", addressContains = "road", contactMobileNumber = "321") should be(List(customerSally))
    findMatching(customerName = "sally", addressContains = "Nowhere") should be('empty)
  }

  test("Search that matches multiple records in database returns correct results") {
    Customer.add(customerDave)
    Customer.add(customerSally)
    findMatching(customerName = "'s") should (have size (2) and
      contain(customerSally) and
      contain(customerDave))
  }
}