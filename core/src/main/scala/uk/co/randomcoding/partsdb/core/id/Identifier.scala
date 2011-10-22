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

/**
 * Companion object for Identifier to match to common identifier types.
 *
 * To match to [[uk.co.randomcoding.partsdb.core.id.DocumentId]]s use the [[uk.co.randomcoding.partsdb.core.document.DocumentId]] object
 */
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

case class CustomerId(id: Long) extends Identifier(id, "CUS")

package document {
/**
 * Sealed base class for specialisations of [[uk.co.randomcoding.partsdb.core.id.Identifier]] for [[uk.co.randomcoding.partsdb.core.document.Document]]s
 */
sealed abstract class DocumentId(val idNum: Long, val docType: DocumentType) extends Identifier(idNum, docType.typeId) {
  override def toString = "%s%d".format(docType.typeId, idNum)
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
}
