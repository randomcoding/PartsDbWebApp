/**
 *
 */
package uk.co.randomcoding.partsdb.core.terms

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 * @deprecated There is no longer types of payment terms
 */
class PaymentTermsMatchTest extends FunSuite with ShouldMatchers {
  /*test("Match to 30 days terms") {
    30 match {
      case PaymentTerms(terms) => terms should be(ThirtyDays)
      case _ => fail()
    }
  }

  test("Match to 60 days terms") {
    60 match {
      case PaymentTerms(terms) => terms should be(SixtyDays)
      case _ => fail()
    }
  }

  test("Match to 90 days terms") {
    90 match {
      case PaymentTerms(terms) => terms should be(NinetyDays)
      case _ => fail()
    }
  }

  test("Match to Custom days terms") {
    List(15, 40, 55, 100) foreach (days => {
      days match {
        case PaymentTerms(terms) => terms should be(CustomTerms(days))
        case _ => fail()
      }
    })
  }*/
}