/**
 *
 */
package uk.co.randomcoding.partsdb.core.document

/**
 * Enumeration of Document Types
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
object DocumentType extends Enumeration { //("INV", "QUO", "ORD", "DEL", "STM", "TRN") {
  type DocType = Value
  val Invoice, Quote, Order, DeliveryNote, Transaction = Value
}
