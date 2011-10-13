/**
 *
 */
package uk.co.randomcoding.partsdb.core.document

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
 */
sealed abstract class Document(val documentId: DocumentId) {
    // add line items

    // add address details
}

/**
 * Document class for an Invoice
 */
case class Invoice(id: InvoiceId) extends Document(id)

/**
 * Document class for a Delivery Note
 */
case class DeliveryNote(id: DeliveryNoteId) extends Document(id)

/**
 * Document class for a Quote
 */
case class Quote(id: QuoteId) extends Document(id)

/**
 * Document class for an Order
 */
case class Order(id: OrderId) extends Document(id)

/**
 * Document class for a Statement
 */
case class Statement(id: StatementId) extends Document(id)