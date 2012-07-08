/*
 * Copyright (C) 2012 RandomCoder <randomcoder@randomcoding.co.uk>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *    RandomCoder - initial API and implementation and/or initial documentation
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

  val contactDave = ContactDetails.create("Dave", "01234 567890", "07890 123456", "dave@email.com", "4455667788", true)
  val contactSally = ContactDetails.create("Sally", "01987 654321", "07654 321098", "sally@snailmail.com", "4455667788", true)

  val customerDave = Customer.create("Dave's Places", daveAddress, 30, contactDave)
  val customerSally = Customer.create("Sally's Scouting", sallyAddress, 30, contactSally)

  override def beforeEach(): Unit = {
    super.beforeEach()

    Seq(daveAddress, sallyAddress) foreach (_ save)
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
    findMatching(contactFax = "4455667788") should (have size (2) and
      contain(customerDave) and
      contain(customerSally))
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
