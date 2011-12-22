/**
 *
 */
package uk.co.randomcoding.partsdb.core.document

import uk.co.randomcoding.partsdb.core.id.Identifier
import uk.co.randomcoding.partsdb.core.id.Identifiable

/**
 * A document is a entity that is stored in the database, and has a document id.
 * It is likely that a document will also be [[uk.co.randomcoding.partsdb.core.document.Printable]] in the future
 *
 * Documents contain line items and address details.
 *
 * @constructor Create a new document instance
 * @param documentId The [[uk.co.randomcoding.partsdb.core.id.Identifier]] of this document.
 * @param documentType The type of the document. This should be one of the values from [[uk.co.randomcoding.partsdb.core.document.DocumentId]]
 * @param lineItems The [[uk.co.randomcoding.partsdb.core.document.LineItem]]s that are in this document
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
case class Document(val documentId: Identifier, val documentType: String, val lineItems: List[LineItem]) extends Identifiable {
  override val identifierFieldName = "documentId"

  /**
   * The printable version of the document id
   */
  lazy val documentNumber = "%s%d".format(documentType, documentId.id)
}