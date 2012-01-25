/**
 *
 */
package uk.co.randomcoding.partsdb.core.document

import net.liftweb.mongodb.record.{ MongoRecord, MongoMetaRecord }
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._

/**
 * `Document`s are the basic objects that make up a [[uk.co.randomcoding.partsdb.core.transaction.Transaction]].
 *
 * Documents basically contain a number of [[uk.co.randomcoding.partsdb.core.document.LineItem]]s and a
 * [[uk.co.randomcoding.partsdb.core.document.DocumentType]].
 *
 * `Document`s can additionally be editable. This indicates that the `Document` has not yet been completed,
 * which in turn indicates the current stage of the [[uk.co.randomcoding.partsdb.core.transaction.Transaction]].
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class Document private () extends MongoRecord[Document] with ObjectIdPk[Document] {
  def meta = Document

  /**
   * The type of document
   */
  object documentType extends EnumField(this, DocumentType)

  /**
   * The numeric identifier of this document.
   *
   * This is maintained separately from the `ObjectId` as it is used to generate the printable document number.
   *
   * This is required to be added and should be unique across all documents, or at least those of the same type.
   */
  object docNumber extends LongField(this)

  /**
   * The line items that are in this document
   */
  object lineItems extends MongoCaseClassListField[Document, LineItem](this)

  /**
   * Is this `Document` editable?
   */
  object editable extends BooleanField(this)

  /**
   * The printable identifier for this document.
   * Comprises the document type string plus the `docNUmber` zero padded to 6 digits.
   *
   * E.g. INV002401
   */
  lazy val documentNumber = "%s%06d".format(documentType.get, docNumber.get)
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
