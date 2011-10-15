/**
 *
 */
package uk.co.randomcoding.partsdb.core.document

/**
 * An id type for [[uk.co.randomcoding.partsdb.core.document.Document]]s.
 *
 * This provides a string type identifier and is used by the case classes defined here.
 *
 * @constructor Create a new instance of a Document Type
 * @param typeId A string that identifies the type of identifier this is.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
sealed class DocumentType(val typeId: String) {
}

object DocumentType {
  def unapply(docType: String): Option[DocumentType] = docType match {
    case "INV" => Some(InvoiceType)
    case "ORD" => Some(OrderType)
    case "QUO" => Some(QuoteType)
    case "DEL" => Some(DeliveryNoteType)
    case "STM" => Some(StatementType)
    case "TRN" => Some(TransactionType)
    case _ => None
  }
}

/**
 * Document type for Invoices
 */
case object InvoiceType extends DocumentType("INV")

/**
 * Document type for Orders
 */
case object OrderType extends DocumentType("ORD")

/**
 * Document type for Quotes
 */
case object QuoteType extends DocumentType("QUO")

/**
 * Document type for Delivery notes
 */
case object DeliveryNoteType extends DocumentType("DEL")

/**
 * Document type for Statements
 */
case object StatementType extends DocumentType("STM")

/**
 * Document type for Transactions
 */
case object TransactionType extends DocumentType("TRN")

/**
 * Null object for document types.
 *
 * This '''should not''' be used for matching
 */
case object NullDocumentType extends DocumentType("NULL")