/**
 *
 */
package uk.co.randomcoding.partsdb.core.document

import uk.co.randomcoding.partsdb.core.id.Identifier

/**
 * Base class for documents.
 *
 * A document is a entity that is stored in the database, and has a document is.
 * It is likely that a document will also be [[uk.co.randomcoding.partsdb.core.document.Printable]] in the future
 *
 * Documents contain line items and address details.
 *
 * Addresses are stored by ID? maybe
 *
 * @constructor Create a new document instance
 * @param documentId The [[uk.co.randomcoding.partsdb.core.document.DocumentId]] of this document. This also identifies its type
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 * @todo Add other details to case class constructor
 */
case class Document(val documentId: Identifier, val documentType: DocumentType) {
  // add line items

  // add address details
}