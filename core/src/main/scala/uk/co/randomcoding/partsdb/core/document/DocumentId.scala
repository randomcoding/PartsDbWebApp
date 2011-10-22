/**
 *
 */
package uk.co.randomcoding.partsdb.core.document

import uk.co.randomcoding.partsdb.core.id.document._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
/**
 * Contains the matching for the different types of [[uk.co.randomcoding.partsdb.code.id.DocumentId]]
 */
object DocumentId {

  private type docTypeTuple = (Long, DocumentType)

  private type docTypeStringTuple = (Long, String)

  /**
   * Extractor based on [[uk.co.randomcoding.partsdb.core.document.DocumentType]] to generate the specific type of identifier.
   *
   * @param tuple A tuple of `('''Long''', '''[[uk.co.randomcoding.partsbd.core.id.DocumentId]]''')`
   * @return A specialised type of [[uk.co.randomcoding.partsdb.core.id.DocumentType]] or [[scala.None]] if no match is made.
   */
  def unapply(tuple: (Long, DocumentType)): Option[DocumentId] = {
    val id = tuple._1
    val documentType = tuple._2

    documentType match {
      case InvoiceType => Some(InvoiceId(id))
      case OrderType => Some(OrderId(id))
      case DeliveryNoteType => Some(DeliveryNoteId(id))
      case QuoteType => Some(QuoteId(id))
      case StatementType => Some(StatementId(id))
      case TransactionType => Some(TransactionId(id))
      case NullDocumentType => None
      case _ => None
    }
  }

  /**
   * Conversion for a String into a [[uk.co.randomcoding.partsbd.core.document.DocumentType]]
   */
  implicit def stringToDocumentType(stringType: String): DocumentType = stringType match {
    case DocumentType(f) => f
    case _ => NullDocumentType
  }
}