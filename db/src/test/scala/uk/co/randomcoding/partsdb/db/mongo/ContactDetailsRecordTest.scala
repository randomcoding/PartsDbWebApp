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
package uk.co.randomcoding.partsdb.db.mongo

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import uk.co.randomcoding.partsdb.core.contact.ContactDetails._
import uk.co.randomcoding.partsdb.core.contact.ContactDetails
import com.foursquare.rogue.Rogue._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class ContactDetailsRecordTest extends FunSuite with ShouldMatchers {

  test("Equals objects are equals and have the same hashcode") {
    val contact1 = create("Dave", "123", "456", "one@two.three", "4455667788", true)
    val contact2 = create("Dave", "123", "456", "one@two.three", "4455667788", true)
    val contact3 = create("Dave", "123", "456", "one@two.three", "4455667788", true)

    contact1 should (be(contact2) and be(contact3))
    contact2 should (be(contact1) and be(contact3))
    contact3 should (be(contact1) and be(contact2))

    contact1.hashCode should (be(contact2.hashCode) and be(contact3.hashCode))
  }

  test("Contact Details are not equals if only the contact name field is different") {
    pending
  }

  test("Contact Details are not equals if only the phone number field is different") {
    pending
  }

  test("Contact Details are not equals if only the mobile number field is different") {
    pending
  }

  test("Contact Details are not equals if only the fax number field is different") {
    pending
  }

  test("Contact Details are not equals if only the email address field is different") {
    pending
  }

  test("Not equals contact details have different hashcodes") {
    pending
  }

  test("Matches does match similar records by contact name and one contact detail") {
    val dave = create("Dave", "01234 567890", "", "", "", true)
    val otherDave = create("Dave", "01234 567890", "em@ai.l", "07777888999", "44556677", true)
    dave matches otherDave should be(true)

    val dave1 = create("Dave1", "", "07777888999", "", "", false)
    val otherDave1 = create("Dave1", "01234 567890", "07777888999", "em@ai.l", "44556677", true)
    dave1 matches otherDave1 should be(true)

    val dave2 = create("Dave2", "", "", "em@ai.l", "", true)
    val otherDave2 = create("Dave2", "01234 567890", "07777888999", "em@ai.l", "44556677", true)
    val differentOtherDave2 = create("Dave2", "01234 567890", "07777888999", "em@ai.l", "44556677", false)
    dave2 matches otherDave2 should be(true)
    dave2 matches differentOtherDave2 should be(true)

    val dave3 = create("Dave3", "", "", "", "44556677", true)
    val otherDave3 = create("Dave3", "01234 567890", "em@ai.l", "07777888999", "44556677", true)
    dave3 matches otherDave3 should be(true)
    otherDave3 matches dave3 should be(true)
  }

  test("Matches does not match details that should not match") {
    pending
  }
}
