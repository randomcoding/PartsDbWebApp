/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.contact._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class DataValidationTest extends FunSuite with ShouldMatchers {
  private val validation = new DataValidation {}

  import validation.validate

  test("Valid address validates ok") {
    val addrItem = ValidationItem(Address.create("Address Name", "An Address in a nice place", "UK"), "Address-1")
    validate(addrItem) should be('empty)
  }

  test("Address with invalid country code fails to validate") {
    val addrItem = ValidationItem(Address.create("Short", "Address Text", "ZZZ"), "Address-2")
    validate(addrItem) should be(Seq(("Country Code ZZZ is not valid")))
  }

  test("Address without short name fails to validate") {
    val addrItem = ValidationItem(Address.create("", "Address Text", "UK"), "Address-3")
    validate(addrItem) should be(Seq(("Address Short Name is not valid")))
  }

  test("Valid Contact Details validate ok") {
    val contactsItem = ValidationItem(ContactDetails.create("Contact", "678", "456", "e@m.l", true), "Contact-1")
    validate(contactsItem) should be('empty)
    val contactsItem1 = ValidationItem(ContactDetails.create("Contact", "7890", "", "", true), "Contact-2")
    validate(contactsItem1) should be('empty)
    val contactsItem2 = ValidationItem(ContactDetails.create("Contact", "", "456", "", true), "Contact-3")
    validate(contactsItem2) should be('empty)
    val contactsItem3 = ValidationItem(ContactDetails.create("Contact", "", "", "e@m.l", true), "Contact-4")
    validate(contactsItem3) should be('empty)
    val contactsItem4 = ValidationItem(ContactDetails.create("Contact", "678", "456", "", true), "Contact-5")
    validate(contactsItem4) should be('empty)
    val contactsItem5 = ValidationItem(ContactDetails.create("Contact", "678", "", "e@m.l", true), "Contact-6")
    validate(contactsItem5) should be('empty)
    val contactsItem6 = ValidationItem(ContactDetails.create("Contact", "", "456", "e@m.l", true), "Contact-7")
    validate(contactsItem6) should be('empty)
  }

  test("Contact Details with no name (or empty name) fail to validate") {
    val contactsItem = ValidationItem(ContactDetails.create("", "678", "456", "e@m.l", true), "Contact-8")
    validate(contactsItem) should be(Seq("Contact requires a name"))
    val contactsItem2 = ValidationItem(ContactDetails.create("  ", "678", "456", "e@m.l", true), "Contact-9")
    validate(contactsItem) should be(Seq("Contact requires a name"))
  }

  test("Contact Details with no phone, mobile or email fails to validate") {
    val contactsItem = ValidationItem(ContactDetails.create("Contact", "", "", "", true), "Contacts-10")
    validate(contactsItem) should be(Seq("Contact Details requires at least one contact method to be entered"))
    val contactsItem1 = ValidationItem(ContactDetails.create("Contact", "", "", "", false), "Contacts-11")
    validate(contactsItem1) should be(Seq("Contact Details requires at least one contact method to be entered"))
  }

  test("Non Empty String validates ok") {
    val stringItem = ValidationItem("Valid String", "")
    validate(stringItem) should be('empty)
  }

  test("Empty or padding only string fails validation") {
    var stringItem = ValidationItem("", "String Field 1")
    validate(stringItem) should be(Seq(("String Field 1 requires a non empty value")))
    stringItem = ValidationItem("  ", "String Field 2")
    validate(stringItem) should be(Seq(("String Field 2 requires a non empty value")))
    stringItem = ValidationItem("\n", "String Field 3")
    validate(stringItem) should be(Seq(("String Field 3 requires a non empty value")))
    stringItem = ValidationItem("\t", "String Field 4")
    validate(stringItem) should be(Seq(("String Field 4 requires a non empty value")))
  }

}