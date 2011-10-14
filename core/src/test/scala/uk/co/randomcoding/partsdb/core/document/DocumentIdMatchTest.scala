/**
 *
 */
package uk.co.randomcoding.partsdb.core.document

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

import uk.co.randomcoding.partsdb.core.document._

/**
 * Tests for the matching capabilities of the [[uk.co.randomcoding.partsdb.core.document.DocumentId]] class
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
class DocumentIdMatchTest extends FunSuite with ShouldMatchers {
    test("Match to Invoice Id") {
        val tuple: Tuple2[Long, DocumentType] = (123L, InvoiceType)
        val id = tuple match {
            case DocumentId(f) => f
            case _ => None
        }

        id should be(InvoiceId(123))

        val id2 = (456L, InvoiceType) match {
            case DocumentId(f) => f
            case _ => None
        }

        id2 should be(InvoiceId(456))
    }

    test("Match to Statement Id") {
        pending
    }

    test("Match to Order Id") {
        pending
    }

    test("Match to Quote Id") {
        pending
    }

    test("Match to Delivery Note Id") {
        pending
    }

    test("Match to Transaction Id") {
        pending
    }
}