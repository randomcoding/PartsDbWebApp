/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.id.DefaultIdentifier
import uk.co.randomcoding.partsdb.core.terms.PaymentTerms
import uk.co.randomcoding.partsdb.core.address.NullAddress

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class DataValidationTest extends FunSuite with ShouldMatchers {
  private val validation = new DataValidation {}

  import validation.validate

  test("Valid address validates ok") {
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
  }

  test("Valid Payment Terms validate") {
    val termsItem = ValidationItem(PaymentTerms(30), "", "")
    validate(termsItem) should be('empty)
  }

  test("Invalid Payment Terms fail to validate") {
    val termsItem = ValidationItem(PaymentTerms(-1), "termsErrorId", "Payment Terms failed validation")
    validate(termsItem) should be(List(("termsErrorId", "Payment Terms failed validation")))
  }
}