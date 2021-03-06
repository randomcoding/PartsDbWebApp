/*
 * Copyright (C) 2012 RandomCoder <randomcoder@randomcoding.co.uk>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *    RandomCoder - initial API and implementation and/or initial documentation
 */
package uk.co.randomcoding.partsdb.core.document

import net.liftweb.mongodb.record.{ MongoRecord, MongoMetaRecord }
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._
import uk.co.randomcoding.partsdb.core.address.Address
import java.util.Date
import com.foursquare.rogue.Rogue._
import uk.co.randomcoding.partsdb.core.transaction.Payment
import uk.co.randomcoding.partsdb.core.system.SystemData

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

  object createdOn extends DateField(this) {
    override val defaultValue = new Date()
  }

  /**
   * The carriage for the items in the document
   */
  object carriage extends DoubleField(this) {
    override val defaultValue = 0d
  }

  /**
   * Is this `Document` editable?
   */
  object editable extends BooleanField(this)

  /**
   * The customer's P/O number if known
   */
  object customerPoReference extends StringField(this, 50)

  /**
   * The [[uk.co.randomcoding.partsdb.core.address.Address]] to which the document is to be delivered/sent
   */
  object documentAddress extends BsonRecordField(this, Address)

  /**
   * The delivery notes that are invoiced in this Document (invoices only)
   */
  object invoicedDeliveryNotes extends BsonRecordListField(this, Document)

  /**
   * Any notes on the document that should be displayed when printed.
   */
  object documentPrintNotes extends StringField(this, 10000)

  /**
   * Get the Customer P/O Reference numbers of the invoiced delivery notes.
   */
  def invoicedCustomerOrderReferences = invoicedDeliveryNotes.get map (_.customerPoReference.get) distinct

  /**
   * The printable identifier for this document.
   * Comprises the document type string plus the `docNumber` zero padded to 6 digits.
   *
   * E.g. INV002401
   */
  lazy val documentNumber = "%s%06d".format(documentType.get, docNumber.get)

  /**
   * Calculate the amount of VAT for this document
   */
  def documentVat: Double = {
    documentAddress.get.country.get match {
      case "United Kingdom" => {
        val vattableCost = lineItems.get.foldLeft(carriage.get)(_ + _.lineCost)

        vattableCost * SystemData.vatRate
      }
      case _ => 0d
    }
  }

  /**
   * Calculates the value of a document
   *
   * This is the sum of the value of the line items and the carriage plus the VAT on that total if applicable
   */
  def documentValue: Double = {
    val lineItemCost = lineItems.get map (_.lineCost) sum

    val subTotal = lineItemCost + carriage.get

    subTotal + documentVat
  }

  /**
   * Calculate the unpaid value of this invoice by getting the payment values of all the
   * [[uk.co.randomcoding.partsdb.core.transaction.InvoicePayment]]s allocated to this invoice from the database.
   *
   * This will assume that if the invoice is closed, it has no outstanding balance.
   *
   * Additionally, if the remaining balance is calculated to be less than 0.001 then this will return ''0.0''
   *
   * @return The outstanding balance for this invoice or ''0.0'' if it is fully paid
   */
  def remainingBalance: Double = {
    if (documentType.get != DocumentType.Invoice) documentValue
    else {
      editable.get match {
        case false => 0.0d
        case true => Payment where (_.paidInvoices subfield (_.paidInvoice) eqs id.get) select (_.paidInvoices) fetch () flatten match {
          case Nil => documentValue
          case payments => {
            val totalPaid = payments filter (_.paidInvoice.get == id.get) map (_.paymentAmount.get) sum

            val outstanding = documentValue - totalPaid

            if (outstanding < 0.001) 0.0 else outstanding
          }
        }
      }
    }
  }

  /**
   * Documents are `equals` if they have the same `documentType`, `docNumber`
   */
  override def equals(that: Any): Boolean = that match {
    case other: Document => documentType.get == other.documentType.get &&
      docNumber.get == other.docNumber.get &&
      customerPoReference.get == other.customerPoReference.get
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
   *
   * @param items The [[uk.co.randomcoding.partsdb.core.document.LineItem]]s to create the document with
   * @param docType The type of document. This is on of the [[uk.co.randomcoding.partsdb.core.document.DocumentType]]s
   * @param carriage The cost of carriage for the document
   * @param customerPoRef The customer's Purchase Order Reference number. This will be assigned to the document in the Order stage.
   *                      Defaults to an empty string so it is not required to be entered for Quotes
   * @param invoicedDeliveryNotes The delivery notes that are being charged . This is only applicable to Invoices and defaults to an empty list
   */
  def create(items: Seq[LineItem], docType: DocumentType.DocType, carriage: Double, customerPoRef: String = "", invoicedDeliveryNotes: Seq[Document] = Nil): Document = {
    require(items.nonEmpty, "Line Items Cannot be empty")
    Document.createRecord.editable(true).documentType(docType).lineItems(items.toList).createdOn(new Date).carriage(carriage).customerPoReference(customerPoRef)
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
  def add(items: Seq[LineItem], docType: DocumentType.DocType, carriage: Double): Option[Document] = {
    add(create(items, docType, carriage))
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
      document.docNumber.get match {
        case -1l => None // force documents with id of -1 to never be matched as they require a new document number
        case docNum => Document.where(_.docNumber eqs docNum).and(_.documentType eqs document.documentType.get).get
      }
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
  def remove(oid: ObjectId) {
    Document where (_.id eqs oid) bulkDelete_!!
  }

  /**
   * Set the document with the given `oid` to not editable
   *
   * @return The updated document in an `Option[`[[uk.co.randomcoding.partsdb.core.document.Document]]`]`
   *         If this returns `None` then there was no document with the given `oid` in the database
   */
  def close(oid: ObjectId): Option[Document] = {
    Document.where(_.id eqs oid).modify(_.editable setTo false) updateMulti

    findById(oid)
  }

  /**
   * Set the `documentPrintNotesField` to the value in `notes` and return the `Option[Document]` with the updated record
   */
  def updateNotes(document: Document, notes: String): Option[Document] = {
    Document where (_.id eqs document.id.get) modify (_.documentPrintNotes setTo notes) updateMulti

    findById(document.id.get)
  }

  /**
   * Update the following fields of the document with the provided `ObjectId` to the values contained in `newDocument`
   *   - carriage
   *   - customer PO reference
   *   - document address
   *   - document print notes
   *   - invoiced delivery notes
   *   - line items
   */
  def update(oid: ObjectId, newDocument: Document): Option[Document] = {
    Document.where(_.id eqs oid).modify(_.carriage setTo newDocument.carriage.get)
      .and(_.customerPoReference setTo newDocument.customerPoReference.get)
      .and(_.documentAddress setTo newDocument.documentAddress.get)
      .and(_.documentPrintNotes setTo newDocument.documentPrintNotes.get)
      .and(_.invoicedDeliveryNotes setTo newDocument.invoicedDeliveryNotes.get)
      .and(_.lineItems setTo newDocument.lineItems.get).updateMulti

    findById(oid)
  }

}

/**
 * Factory object for creating instances of `Document`s with a fixed `DocumentType`
 */
sealed abstract class DocumentInstance(docType: DocumentType.DocType) {
  /**
   * Convenience method to create a new document instance.
   *
   * This delegates to [[uk.co.randomcoding.partsdb.core.document.DocumentInstance#create(Seq[LineItem],Double,String)]]
   */
  def apply(items: Seq[LineItem], carriage: Double, customerPoRef: String = "", invoicedDeliveryNotes: Seq[Document] = Nil): Document = create(items, carriage, customerPoRef, invoicedDeliveryNotes)

  /**
   * Convenience method to create a new document instance.
   *
   * This delegates to [[uk.co.randomcoding.partsdb.core.document.Document#create(Seq[LineItem],DocumentType.DocType,Double,String)]]
   */
  def create(items: Seq[LineItem], carriage: Double, customerPoRef: String = "", invoicedDeliveryNotes: Seq[Document] = Nil): Document = Document.create(items, docType, carriage, customerPoRef, invoicedDeliveryNotes)

  /**
   * Convenience method to create a new document instance and add it to the database
   *
   * This delegates to [[uk.co.randomcoding.partsdb.core.document.Document#add(Seq[LineItem],DocumentType.DocType,Double,String)]]
   */
  def add(items: Seq[LineItem], carriage: Double, customerPoRef: String = "", invoicedDeliveryNotes: Seq[Document] = Nil): Option[Document] = Document.add(create(items, carriage, customerPoRef, invoicedDeliveryNotes))
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
