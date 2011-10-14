/**
 *
 */
package uk.co.randomcoding.partsdb.core.document

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

import uk.co.randomcoding.partsdb.core.document._
import uk.co.randomcoding.partsdb.core.document.DocumentId._

/**
 * Tests for the matching capabilities of the [[uk.co.randomcoding.partsdb.core.document.DocumentId]] class
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
class DocumentIdMatchTest extends FunSuite with ShouldMatchers {
    test("Match to Invoice Id") {
        validateMatch(123, InvoiceType, InvoiceId(123))
    }

    test("Match to Statement Id") {
        validateMatch(321, StatementType, StatementId(321))
    }

    test("Match to Order Id") {
        validateMatch(567, "ORD", OrderId(567))
    }

    test("Match to Quote Id") {
        validateMatch(890, "QUO", QuoteId(890))
    }

    test("Match to Delivery Note Id") {
        validateMatch(765, "DEL", DeliveryNoteId(765))
    }

    test("Match to Transaction Id") {
        validateMatch(476, "TRN", TransactionId(476))
    }

    test("Match to Invalid Id") {
        val dType: DocumentType = "BAD"
        (908L, dType) match {
            case DocumentId(dId) => fail()
            case _ => // pass
        }
    }

    private val validateMatch = (id: Long, dType: DocumentType, expected: DocumentId) => {
        (id, dType) match {
            case DocumentId(dId) => dId should be(expected)
            case _ => fail()
        }
    }
}