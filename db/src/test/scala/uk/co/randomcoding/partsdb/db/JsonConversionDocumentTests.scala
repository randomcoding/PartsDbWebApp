/**
 *
 */
package uk.co.randomcoding.partsdb.db

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import uk.co.randomcoding.partsdb.core.document.Document
import uk.co.randomcoding.partsdb.core.id.Identifier
import uk.co.randomcoding.partsdb.core.document.DocumentType._
import uk.co.randomcoding.partsdb.core.document.LineItem
import uk.co.randomcoding.partsdb.core.transaction.Transaction

/**
 * This test class '''should''' test all the different document types that are used.
 *
 * However, if a document is essentially the same for all types, then this test is required to test
 * each of the different forms of document that are used.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 * @deprecated(There is no longer any need to do JSON conversions)
 */
class JsonConversionDocumentTests extends JsonConversionTesting {

  /*ignore("Can convert Quote to JSON") {
    fail("DB Access has changed")
    val quoteJson: String = Document(Identifier(321), Quote, List(LineItem(1, Identifier(456), 1, 25.00, 0.25)), Identifier(909))
    quoteJson should be("""{"documentId":{"id":321},"documentType":"QUO","lineItems":[{"lineNumber":1,"partId":{"id":456},"quantity":1,"basePrice":25.0,"markup":0.25}],"transactionId":{"id":909}}""")

    val quote2Json: String = Document(Identifier(321), Quote, List(LineItem(1, Identifier(456), 1, 25.00, 0.25), LineItem(2, Identifier(789), 4, 45.01, 0.1)), Identifier(101))
    quote2Json should be("""{"documentId":{"id":321},"documentType":"QUO",""" +
      """"lineItems":[{"lineNumber":1,"partId":{"id":456},"quantity":1,"basePrice":25.0,"markup":0.25},{"lineNumber":2,"partId":{"id":789},"quantity":4,"basePrice":45.01,"markup":0.1}],"transactionId":{"id":101}}""")
  }

  ignore("Can convert JSON to Quote") {
    fail("DB Access has changed")
    val quoteJson: String = """{"documentId":{"id":321},"documentType":"QUO",""" +
      """"lineItems":[{"lineNumber":1,"partId":{"id":456},"quantity":1,"basePrice":25.0,"markup":0.25},{"lineNumber":2,"partId":{"id":789},"quantity":4,"basePrice":45.01,"markup":0.1}],"transactionId":{"id":202}}"""
    val quote = Document(Identifier(321), Quote, List(LineItem(1, Identifier(456), 1, 25.00, 0.25), LineItem(2, Identifier(789), 4, 45.01, 0.1)), Identifier(202))
    checkJsonConversion[Document](quoteJson, quote)
  }

  ignore("Can convert Order to JSON") {
    pending
  }

  ignore("Can convert JSON to Order") {
    pending
  }

  ignore("Can convert Invoice to JSON") {
    pending
  }

  ignore("Can convert JSON to Invoice") {
    pending
  }

  ignore("Can convert Delivery Note to JSON") {
    pending
  }

  ignore("Can convert JSON to Delivery Note") {
    pending
  }

  ignore("Can convert Transaction to JSON") {
    fail("DB Access has changed")
    val emptyTrans: String = Transaction(Identifier(345), Identifier(3))
    emptyTrans should be("""{"transactionId":{"id":345},"customerId":{"id":3}}""")

    val fullTrans: String = Transaction(Identifier(456), Identifier(5), Some(Set(Identifier(234), Identifier(345))))
    fullTrans should be("""{"transactionId":{"id":456},"customerId":{"id":5},"documents":[{"id":234},{"id":345}]}""")
  }

  ignore("Can convert JSON to Transaction") {
    fail("DB Access has changed")
    val fullTransJson = """{"transactionId":{"id":456},"customerId":{"id":5},"documents":[{"id":234},{"id":345}]}"""
    val fullTrans = Transaction(Identifier(456), Identifier(5), Some(Set(Identifier(234), Identifier(345))))
    checkJsonConversion[Transaction](fullTransJson, fullTrans)

    val emptyTransJson = """{"transactionId":{"id":345},"customerId":{"id":3}}"""
    val emptyTrans = Transaction(Identifier(345), Identifier(3))
    checkJsonConversion[Transaction](emptyTransJson, emptyTrans)
  }*/
}