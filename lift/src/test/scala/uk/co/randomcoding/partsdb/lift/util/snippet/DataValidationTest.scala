/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.id.DefaultIdentifier
import uk.co.randomcoding.partsdb.core.terms.PaymentTerms
import uk.co.randomcoding.partsdb.core.contact._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class DataValidationTest extends FunSuite with ShouldMatchers {
  private val validation = new DataValidation {}

  import validation.validate

  /*test("Valid address validates ok") {
    val addrItem = ValidationItem(Address(DefaultIdentifier, "Short", "Address Text", "UK"), "", "")
    validate(addrItem) should be('empty)
  }

  test("NullAddress fails to validate") {
    val addrItem = ValidationItem(NullAddress, "addressErrorId", "Address failed validation")
    validate(addrItem) should be(List(("addressErrorId", "Address failed validation")))
  }

  test("Address with invalid country code fails to validate") {
    val addrItem = ValidationItem(Address(DefaultIdentifier, "Short", "Address Text", "ZZZ"), "addressErrorId", "Address failed validation")
    validate(addrItem) should be(List(("addressErrorId", "Address failed validation")))
  }*/

  /* test("Valid Payment Terms validate") {
    val termsItem = ValidationItem(PaymentTerms(30), "", "")
    validate(termsItem) should be('empty)
  }

  test("Invalid Payment Terms fail to validate") {
    val termsItem = ValidationItem(PaymentTerms(-1), "termsErrorId", "Payment Terms failed validation")
    validate(termsItem) should be(List(("termsErrorId", "Payment Terms failed validation")))
  }*/

  test("Valid Contact Details validate ok") {
    /*val contactsItem = ValidationItem(ContactDetails("Contact", Some(List(Phone("678"))), Some(List(Mobile("456"))), Some(List(Email("e@m.l")))), "", "")
    validate(contactsItem) should be('empty)
    val contactsItem1 = ValidationItem(ContactDetails("Contact", phoneNumbers = Some(List(Phone("678")))), "", "")
    validate(contactsItem1) should be('empty)
    val contactsItem2 = ValidationItem(ContactDetails("Contact", mobileNumbers = Some(List(Mobile("456")))), "", "")
    validate(contactsItem2) should be('empty)
    val contactsItem3 = ValidationItem(ContactDetails("Contact", emailAddresses = Some(List(Email("e@m.l")))), "", "")
    validate(contactsItem3) should be('empty)
    val contactsItem4 = ValidationItem(ContactDetails("Contact", Some(List(Phone("678"))), Some(List(Mobile("456")))), "", "")
    validate(contactsItem4) should be('empty)
    val contactsItem5 = ValidationItem(ContactDetails("Contact", Some(List(Phone("678"))), None, Some(List(Email("e@m.l")))), "", "")
    validate(contactsItem5) should be('empty)
    val contactsItem6 = ValidationItem(ContactDetails("Contact", None, Some(List(Mobile("456"))), Some(List(Email("e@m.l")))), "", "")
    validate(contactsItem6) should be('empty)*/
  }

  test("Contact Details with no name (or empty name) fail to validate") {
    /*val contactsItem = ValidationItem(ContactDetails("", Some(List(Phone("678"))), Some(List(Mobile("456"))), Some(List(Email("e@m.l")))), "contactsError", "Contacts failed validation")
    validate(contactsItem) should be(List(("contactsError", "Contacts failed validation")))
    val contactsItem2 = ValidationItem(ContactDetails("  ", Some(List(Phone("678"))), Some(List(Mobile("456"))), Some(List(Email("e@m.l")))), "contactsError", "Contacts failed validation")
    validate(contactsItem) should be(List(("contactsError", "Contacts failed validation")))*/
  }

  test("Contact Details with no phone, mobile or email fails to validate") {
    /*val contactsItem = ValidationItem(ContactDetails("Contact"), "contactsError", "Contacts failed validation")
    validate(contactsItem) should be(List(("contactsError", "Contacts failed validation")))
    val contactsItem1 = ValidationItem(ContactDetails("Contact", Some(List.empty[Phone]), Some(List.empty[Mobile]), Some(List.empty[Email])), "contactsError", "Contacts failed validation")
    validate(contactsItem1) should be(List(("contactsError", "Contacts failed validation")))*/
  }

  test("Non Empty String validates ok") {
    val stringItem = ValidationItem("Valid String", "", "")
    validate(stringItem) should be('empty)
  }

  test("Empty or padding only string fails validation") {
    var stringItem = ValidationItem("", "stringError", "String failed validation")
    validate(stringItem) should be(List(("stringError", "String failed validation")))
    stringItem = ValidationItem("  ", "stringError", "String failed validation")
    validate(stringItem) should be(List(("stringError", "String failed validation")))
    stringItem = ValidationItem("\n", "stringError", "String failed validation")
    validate(stringItem) should be(List(("stringError", "String failed validation")))
    stringItem = ValidationItem("\t", "stringError", "String failed validation")
    validate(stringItem) should be(List(("stringError", "String failed validation")))
  }

}