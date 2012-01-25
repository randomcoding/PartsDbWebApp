/**
 *
 */
package uk.co.randomcoding.partsdb.core.document

import uk.co.randomcoding.partsdb.core.id.{ Identifier, Identifiable }
import uk.co.randomcoding.partsdb.core.transaction.Transaction
import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field.EnumField

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
 * @param transactionId The identifier of the [[uk.co.randomcoding.partsdb.core.transaction.Transaction]] that contains this document
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class Document extends MongoRecord[Document] with ObjectIdPk[Document] {
  def meta = Document

  object documentType extends EnumField(this, DocumentType)
  object lineItems extends MongoCaseClassListField[Document, LineItem](this)
  object transactionId extends ObjectIdRefField(this, Transaction)
}

object Document extends Document with MongoMetaRecord[Document]

/*case class Document(val documentId: Identifier, val documentType: String, val lineItems: List[LineItem], transactionId: Identifier) extends Identifiable {
  override val identifierFieldName = "documentId"

  */
/**
 * The printable version of the document id
 */ /*
  lazy val documentNumber = "%s%d".format(documentType, documentId.id)
}*/
