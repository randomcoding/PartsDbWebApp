/**
 *
 */
package uk.co.randomcoding.partsdb.core.document

import uk.co.randomcoding.partsdb.core.part.Part
import uk.co.randomcoding.partsdb.core.util.MongoFieldHelpers._

import net.liftweb.mongodb.record.{ BsonMetaRecord, BsonRecord }
import net.liftweb.record.field._
import net.liftweb.mongodb.record.field.ObjectIdRefField

/**
 * A line item for documents.
 *
 * This is created as a `BsonRecord` to allow it to be embedded directly into the dosument object rather than referenced by an id
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class LineItem private () extends BsonRecord[LineItem] {

  def meta = LineItem

  object lineNumber extends IntField(this)
  object partId extends ObjectIdRefField(this, Part)
  object quantity extends IntField(this)
  object basePrice extends DoubleField(this)
  object markup extends DoubleField(this)

  /**
   * Calculates the cost of the line item.
   *
   * This is done by
   * {{{
   * (basePrice + (basePrice * markup)) * quantity
   * }}}
   *
   * @return A double value which is the total cost (pre tax) for this line item
   */
  final def lineCost: Double = (basePrice + (basePrice * markup)) * quantity

}

object LineItem extends LineItem with BsonMetaRecord[LineItem] {
  /**
   * Create a new `LineItem` but '''do not''' save it to the database
   *
   * @param lineNumber The index of the line
   * @param partId The `ObjectId` of the [[uk.co.randomcoding.partsdb.core.part.Part]] that this line item is selling
   * @param quantity The number of parts in this line item
   * @param basePrice The base price of the part being sold. This is derived from the [[uk.co.randomcoding.partsdb.core.supplier.Supplier]]'s price for the part
   * @param markup The percentage markup to apply to the base price. This derives the total, pre tax, price per part for this line item.
   * This is usually in the range 0.0 to 1.0 where 1.0 is a 100% markup.
   *
   * @return The created `LineItem`
   */
  def create(lineNumber: Int, part: Part, quantity: Int, basePrice: Double, markup: Double): LineItem = {
    LineItem.createRecord.lineNumber(lineNumber).partId(part.id.get).quantity(quantity).basePrice(basePrice).markup(markup)
  }
}