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

import com.foursquare.rogue.Rogue._
import uk.co.randomcoding.partsdb.core.part.{ PartKit, Part }
import uk.co.randomcoding.partsdb.core.supplier.Supplier
import uk.co.randomcoding.partsdb.core.util.MongoFieldHelpers.{ doubleFieldToDouble, intFieldToDouble }
import net.liftweb.record.field._
import net.liftweb.mongodb.record.{ BsonRecord, BsonMetaRecord }
import net.liftweb.mongodb.record.field.ObjectIdRefField
import net.liftweb.common.Logger

/**
 * A line item for documents.
 *
 * This is created as a `BsonRecord` to allow it to be embedded directly into the document object rather than referenced by an id
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
  object partSupplier extends ObjectIdRefField(this, Supplier) {
    override val defaultValue = null
  }

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

  /**
   * The cost price of the line item without markup.
   */
  final def costPrice: Double = basePrice * quantity

  override def equals(that: Any): Boolean = that match {
    case other: LineItem => {
      lineNumber.get == other.lineNumber.get &&
        partId.get == other.partId.get &&
        quantity.get == other.quantity.get &&
        basePrice.get == other.basePrice.get &&
        markup.get == other.markup.get
    }
    case _ => false
  }

  private[this] val hashCodeFields = Seq(lineNumber, partId, quantity, basePrice, markup)

  override def hashCode: Int = getClass.hashCode() + hashCodeFields.foldLeft(0)(_ + _.get.hashCode)

}

object LineItem extends LineItem with BsonMetaRecord[LineItem] with Logger {

  /**
   * Create a new `LineItem` for a [[uk.co.randomcoding.partsdb.core.part.Part]]
   *
   * @param lineNumber The index of the line
   * @param part The [[uk.co.randomcoding.partsdb.core.part.Part]] that this line item is selling
   * @param quantity The number of parts in this line item
   * @param basePrice The base price of the part being sold. This is derived from the [[uk.co.randomcoding.partsdb.core.supplier.Supplier]]'s price for the part
   * @param markup The percentage markup to apply to the base price. This derives the total, pre tax, price per part for this line item.
   * This is usually in the range 0.0 to 1.0 where 1.0 is a 100% markup.
   * @param partSupplier The [[uk.co.randomcoding.partsdb.core.supplier.Supplier]] of the part in the line item.
   *
   * @return The created `LineItem`
   */
  def apply(lineNumber: Int, part: Part, quantity: Int, basePrice: Double, markup: Double, partSupplier: Supplier): LineItem = {
    create(lineNumber, part, quantity, basePrice, markup, partSupplier)
  }

  /**
   * Create a new `LineItem` for a [[uk.co.randomcoding.partsdb.core.part.PartKit]]
   *
   * @param lineNumber The index of the line
   * @param part The [[uk.co.randomcoding.partsdb.core.part.Part]] that this line item is selling
   * @param quantity The number of parts in this line item
   * @param basePrice The base price of the part being sold. This is derived from the [[uk.co.randomcoding.partsdb.core.supplier.Supplier]]'s price for the part
   * @param markup The percentage markup to apply to the base price. This derives the total, pre tax, price per part for this line item.
   * This is usually in the range 0.0 to 1.0 where 1.0 is a 100% markup.
   *
   * @return The created `LineItem`
   *
   * @throws LineItemCreationException if the [[uk.co.randomcoding.partsdb.core.supplier.Supplier]] record for '''C.A.T.9''' cannot be found
   */
  @throws(classOf[LineItemCreationException])
  def apply(lineNumber: Int, partKit: PartKit, quantity: Int, basePrice: Double, markup: Double): LineItem = {
    create(lineNumber, partKit, quantity, basePrice, markup)
  }

  /**
   * Create a new `LineItem` for a [[uk.co.randomcoding.partsdb.core.part.Part]]
   *
   * @param lineNumber The index of the line
   * @param part The [[uk.co.randomcoding.partsdb.core.part.Part]] that this line item is selling
   * @param quantity The number of parts in this line item
   * @param basePrice The base price of the part being sold. This is derived from the [[uk.co.randomcoding.partsdb.core.supplier.Supplier]]'s price for the part
   * @param markup The percentage markup to apply to the base price. This derives the total, pre tax, price per part for this line item.
   * This is usually in the range 0.0 to 1.0 where 1.0 is a 100% markup.
   * @param partSupplier The [[uk.co.randomcoding.partsdb.core.supplier.Supplier]] of the part in the line item.
   *
   * @return The created `LineItem`
   */
  def create(lineNumber: Int, part: Part, quantity: Int, basePrice: Double, markup: Double, partSupplier: Supplier): LineItem = {
    LineItem.createRecord.lineNumber(lineNumber).partId(part.id.get).quantity(quantity).basePrice(basePrice).markup(markup).partSupplier(partSupplier.id.get)
  }

  /**
   * Create a new `LineItem` for a [[uk.co.randomcoding.partsdb.core.part.PartKit]]
   *
   * @param lineNumber The index of the line
   * @param part The [[uk.co.randomcoding.partsdb.core.part.Part]] that this line item is selling
   * @param quantity The number of parts in this line item
   * @param basePrice The base price of the part being sold. This is derived from the [[uk.co.randomcoding.partsdb.core.supplier.Supplier]]'s price for the part
   * @param markup The percentage markup to apply to the base price. This derives the total, pre tax, price per part for this line item.
   * This is usually in the range 0.0 to 1.0 where 1.0 is a 100% markup.
   *
   * @return The created `LineItem`
   *
   * @throws LineItemCreationException if the [[uk.co.randomcoding.partsdb.core.supplier.Supplier]] record for '''C.A.T.9''' cannot be found
   */
  @throws(classOf[LineItemCreationException])
  def create(lineNumber: Int, partKit: PartKit, quantity: Int, basePrice: Double, markup: Double): LineItem = {
    val cat9 = Supplier.where(_.supplierName startsWith "C.A.T.").get
    cat9 match {
      case Some(s) => LineItem.createRecord.lineNumber(lineNumber).partId(partKit.id.get).quantity(quantity).basePrice(basePrice).markup(markup).partSupplier(s.id.get)
      case _ => {
        val errorMessage = "Failed to find C.A.T.9 Supplier record. Cannot create a Part Kit Line Item"
        error(errorMessage)
        throw new LineItemCreationException(errorMessage)
      }
    }
  }
}
