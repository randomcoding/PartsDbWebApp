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

  private val DefaultDocumentId = -1l

  /**
   * The type of document
   */
  object documentType extends EnumNameField(this, DocumentType)

  /**
   * The numeric identifier of this document.
   *
   * This is maintained separately from the `ObjectId` as it is used to generate the printable document number.
   *
   * This is required to be added and should be unique across all documents, or at least those of the same type.
   */
  object docNumber extends LongField(this) {
    override def defaultValue = -1l
  }

  /**
   * The line items that are in this document
   */
  object lineItems extends BsonRecordListField(this, LineItem) {
    override def required_? = false
  }

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

  /**
   * Documents are `equals` if they have the same `documentType`, `docNumber`
   */
  override def equals(that: Any): Boolean = that match {
    case other: Document => documentType.get == other.documentType.get && docNumber.get == other.docNumber.get
    case _ => false
  }

  override def hashCode: Int = getClass.hashCode + documentType.get.hashCode + docNumber.get.hashCode
}

object Document extends Document with MongoMetaRecord[Document] {
  import com.foursquare.rogue.Rogue._
  import org.bson.types.ObjectId

  private def docNum = Document.where(_.id exists true).count + 1

  /**
   * Create a new document with the given type but '''do not''' add it to the database.
   *
   * This also '''does not''' assign a document number as this is done in the `add` method.
   *
   * The `editable` field is set to true
   */
  def create(items: Seq[LineItem], docType: DocumentType.DocType): Document = {
    require(items.nonEmpty, "Line Items Cannot be empty")
    Document.createRecord.editable(true).documentType(docType).lineItems(items.toList)
  }

  /**
   * Create a new document and add it to the database
   *
   * If there is a matching document present, then the match will be returned and no add operation will be performed.
   * If there is no matching document, then this will assign a new document id to the document if its current
   * document number is 0 or less and then attempt to add it to the database.
   *
   * @return An optional `Document` object. This will be filled if the add succeeded or `None` if not
   */
  def add(items: Seq[LineItem], docType: DocumentType.DocType): Option[Document] = {
    add(create(items, docType))
  }

  /**
   * Create a new document and add it to the database unless a matching document is found
   *
   * If there is a matching document present, then the match will be returned and no add operation will be performed.
   * If there is no matching document, then this will assign a new document id to the document if its current
   * document number is 0 or less and then attempt to add it to the database.
   *
   * @return An optional `Document` object. This will be filled if the add succeeded or `None` if not
   */
  def add(doc: Document): Option[Document] = {
    findMatching(doc) match {
      case Some(d) => Some(d)
      case _ => {
        val d = if (doc.docNumber.get == DefaultDocumentId) doc.docNumber(DocumentId.nextId().currentId.get) else doc
        d.save match {
          case document: Document => Some(document)
          case _ => None
        }
      }
    }
  }

  /**
   * Find a document that matches this one.
   *
   * Documents match if they have the same object id or have the same document number and documentType
   *
   * If multiple document match, then the ''first'' match is returned in an `Option`. Note ''first'' is non deterministic
   */
  def findMatching(document: Document): Option[Document] = findById(document.id.get) match {
    case Some(d) => Some(d)
    case _ => {
      val doc = Document.where(_.docNumber eqs document.docNumber.get).and(_.documentType eqs document.documentType.get).get
      doc
    }
  }

  /**
   * Find the `Document` that has the given `ObjectId` or `None` if there is not one
   */
  def findById(oid: ObjectId): Option[Document] = Document where (_.id eqs oid) get

  def findByDocumentNumber(docNumber: Long): Option[Document] = Document where (_.docNumber eqs docNumber) get

  /**
   * Remove the Document with the given id from the database
   */
  def remove(oid: ObjectId): Unit = Document where (_.id eqs oid) bulkDelete_!!
}

/**
 * Factory object for creating instances of `Document`s with a fixed `DocumentType`
 */
sealed abstract class DocumentInstance(docType: DocumentType.DocType) {
  def create(items: List[LineItem]): Document = Document.create(items, docType)

  def add(items: List[LineItem]): Option[Document] = Document.add(create(items))
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
