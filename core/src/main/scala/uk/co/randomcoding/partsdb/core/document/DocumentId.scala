/**
 *
 */
package uk.co.randomcoding.partsdb.core.document
import uk.co.randomcoding.partsdb.core.id.Identifier

/**
 * An Identifier for a document.
 *
 * This is composed of a unique number and a type identifier (Invoide, Quote etc.)
 *
 * @constructor
 * @param idNum The (unique) id of this document
 * @param doctype The [[uk.co.randomcoding/partsdb.core.document.DocumentType]] for this id
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
sealed abstract class DocumentId(val idNum: Long, val docType: DocumentType) extends Identifier(idNum, docType.typeId) {

    /**
     * Pattern match based on type
     */
    def unapply(id: Long, documentType: DocumentType): Option[DocumentId] = documentType match {
        case InvoiceType => Some(InvoiceId(id))
        case OrderType => Some(OrderId(id))
        case DeliveryNoteType => Some(DeliveryNoteId(id))
        case QuoteType => Some(QuoteId(id))
        case StatementType => Some(StatementId(id))
        case _ => None
    }

    override def toString = "%s%d".format(docType, idNum)
}

/**
 * Id type for invoices.
 */
case class InvoiceId(id: Long) extends DocumentId(id, InvoiceType)

/**
 * Id type for Orders.
 */
case class OrderId(id: Long) extends DocumentId(id, OrderType)

/**
 * Id type for Delivery Notes.
 */
case class DeliveryNoteId(id: Long) extends DocumentId(id, DeliveryNoteType)

/**
 * Id type for Quotes.
 */
case class QuoteId(id: Long) extends DocumentId(id, QuoteType)

/**
 * Id type for Statements.
 */
case class StatementId(id: Long) extends DocumentId(id, StatementType)