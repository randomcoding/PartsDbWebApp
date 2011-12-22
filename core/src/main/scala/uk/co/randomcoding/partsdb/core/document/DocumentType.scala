/**
 *
 */
package uk.co.randomcoding.partsdb.core.document

/**
 * An id type for [[uk.co.randomcoding.partsdb.core.document.Document]]s.
 *
 * This provides string identifiers for the different document types.
 *
 * @constructor Create a new instance of a Document Type
 * @param typeId A string that identifies the type of identifier this is.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
object DocumentType {
  val Invoice = "INV"
  val Quote = "QUO"
  val Order = "ORD"
  val DeliveryNote = "DEL"
  val Statement = "STM"
  val Transaction = "TRN"
}
