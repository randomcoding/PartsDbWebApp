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
  object docNumber extends LongField(this) {
    override val defaultValue = -1l
  }

  /**
   * The line items that are in this document
   */
  object lineItems extends BsonRecordListField(this, LineItem)

  /**
   * Is this `Document` editable?
   */
  object editable extends BooleanField(this)

  /**
   * The printable identifier for this document.
   * Comprises the document type string plus the `docNumber` zero padded to 6 digits.
   *
   * E.g. INV002401
   */
  lazy val documentNumber = "%s%06d".format(documentType.get, docNumber.get)
}

object Document extends Document with MongoMetaRecord[Document] {
  import com.foursquare.rogue.Rogue._

  private def docNum = Document.where(_.id exists true).count + 1

  /**
   * Create a new document with the given type but '''do not''' add it to the database.
   *
   * This also '''does not''' assign a document number as this is done in the `add` method
   */
  def create(items: Seq[LineItem], docType: DocumentType.DocType): Document = {
    Document.createRecord.editable(true).documentType(docType).lineItems(items.toList)
  }

  /**
   * Create a new document and add it to the database
   *
   * This will assign a new document id to the document if its current document number is 0 or less.
   *
   * @return An optional `Document` object. This will be filled if the add succeeded or `None` if not
   */
  def add(items: Seq[LineItem], docType: DocumentType.DocType): Option[Document] = {
    add(create(items, docType))
  }

  /**
   * Create a new document and add it to the database
   *
   * This will assign a new document id to the document if its current document number is 0 or less.
   *
   * @return An optional `Document` object. This will be filled if the add succeeded or `None` if not
   */
  def add(doc: Document): Option[Document] = {
    val d = if (doc.docNumber.get <= 0) doc.docNumber(docNum) else doc

    d.save match {
      case document: Document => Some(document)
      case _ => None
    }
  }
}

/**
 * Factory object for creating instances of `Document`s with a fixed `DocumentType`
 */
sealed abstract class DocumentInstance(docType: DocumentType.DocType) {
  def create(items: Seq[LineItem]): Document = Document.create(items, docType)

  def add(items: Seq[LineItem]): Option[Document] = Document.add(create(items))
}

/**
 * Factory for creating Quote Document Instances
 */
object Quote extends DocumentInstance(DocumentType.Quote)

/**
 * Factory for creating Invoice Document Instances
 */
object Invoice extends DocumentInstance(DocumentType.Invoice)

/**
 * Factory for creating Order Document Instances
 */
object Order extends DocumentInstance(DocumentType.Order)

/**
 * Factory for creating Delivery Note Document Instances
 */
object DeliveryNote extends DocumentInstance(DocumentType.DeliveryNote)
