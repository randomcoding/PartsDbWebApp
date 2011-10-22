/**
 *
 */
package uk.co.randomcoding.partsdb.core.id

import net.liftweb.common.Logger
import uk.co.randomcoding.partsdb.core.document.DocumentType._
import uk.co.randomcoding.partsdb.core.document._

/**
 * BAse class for identifier types.
 *
 * ***This Should really use a mthod to get a new unique id number I think***
 *
 * @constructor Create a new (hopefully) unique identifier
 * @param uniqueId A long that should be unique amongst all ids
 * @param identifierType A string denoting the type of identifier this is. Can be used by case classes to indicate sub types of identifiers. This defaults to an empty string for non document type objects.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
sealed abstract class Identifier(val uniqueId: Long, val identifierType: String = "") {
  override def toString: String = "Identifier: %s%d".format(identifierType, uniqueId)

  override def hashCode: Int = getClass.hashCode + identifierType.hashCode + uniqueId.hashCode

  override def equals(that: Any): Boolean = {
    that.isInstanceOf[Identifier] match {
      case true => {
        val other = that.asInstanceOf[Identifier]
        uniqueId == other.uniqueId && identifierType == other.identifierType
      }
      case false => false
    }
  }
}

object Identifier extends Logger {
  def apply(id: Long, idType: String): Option[Identifier] = {
    idType match {
      case "CUS" => Some(CustomerId(id))
      case _ => {
        error("Unknown Id Type: %s".format(idType))
        None
      }
    }
  }
}

sealed abstract class DocumentId(val idNum: Long, val docType: DocumentType) extends Identifier(idNum, docType.typeId) {
  override def toString = "%s%d".format(docType.typeId, idNum)
}

object DocumentId {

  type docTypeTuple = (Long, DocumentType)

  type docTypeStringTuple = (Long, String)

  /**
   * Extractor based on [[uk.co.randomcoding.partsdb.core.document.DocumentType]] to generate the specific type of identifier.
   *
   * @param tuple A tuple of `('''Long''', '''[[uk.co.randomcoding.partsbd.core.document.DocumentId]]''')`
   * @return A specialised type of [[uk.co.randomcoding.partsdb.core.document.DocumentType]] or [[scala.None]] if no match is made.
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

  implicit def stringToDocumentType(stringType: String): DocumentType = stringType match {
    case DocumentType(f) => f
    case _ => NullDocumentType
  }
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

/**
 * Id type for Transactions.
 */
case class TransactionId(id: Long) extends DocumentId(id, TransactionType)

case class CustomerId(id: Long) extends Identifier(id, "CUS")