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
   * The line items that are in this document
   */
  object lineItems extends MongoCaseClassListField[Document, LineItem](this)

  //object transaction extends ObjectIdRefField(this, Transaction)

  /**
   * Is this `Document` editable?
   */
  object editable extends BooleanField(this)

  /**
   * The printable identifier for this document.
   * Comprises the document type string plus the ObjectId as a string
   */
  lazy val documentNumber = "%s-%s".format(documentType.get, id.asString)
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
