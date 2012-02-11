/**
 *
 */
package uk.co.randomcoding.partsdb.core.document

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import uk.co.randomcoding.partsdb.core.document.DocumentType._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class DocumentTest extends FunSuite with ShouldMatchers {
  test("Generation of printable Document Number") {
    val doc = Document.createRecord.documentType(DocumentType.Invoice).docNumber(10001).editable(true)

    doc.documentNumber should be("INV010001")
  }
}