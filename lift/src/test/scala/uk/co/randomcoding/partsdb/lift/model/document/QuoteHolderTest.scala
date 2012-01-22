/**
 *
 */
package uk.co.randomcoding.partsdb.lift.model.document

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import uk.co.randomcoding.partsdb.core.part.Part
import uk.co.randomcoding.partsdb.core.id.Identifier
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle
import uk.co.randomcoding.partsdb.core.document.LineItem

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class QuoteHolderTest extends FunSuite with ShouldMatchers {

  private def quoteHolder = new QuoteHolder()

  private val vehicle = Vehicle(Identifier(345), "Vehicle 1")
  private val part1 = Part(Identifier(123), "Part 1", 10.0, Some(vehicle))
  private val part2 = Part(Identifier(234), "Part 2", 20.0, Some(vehicle))

  test("Empty Quote Holder generates correct empty line items list") {
    quoteHolder.lineItems should be(Nil)
  }

  test("Adding a line item with zero quantity to an empty quote holder generates an empty line items list") {
    val holder = quoteHolder
    holder.currentPart(Some(part1))
    holder.addLineItem(0)

    holder.lineItems should be(Nil)
  }

  test("Adding a line item with negative quantity to an empty quote holder generates an empty line items list") {
    val holder = quoteHolder
    holder.currentPart(Some(part1))
    holder.addLineItem(-100)

    holder.lineItems should be(Nil)
  }

  test("Adding single part to holder generates correct line item") {
    val holder = quoteHolder
    holder.currentPart(Some(part1))
    holder.addLineItem(3)

    holder.lineItems should be(List(LineItem(0, Identifier(123), 3, 10.0, 0.25)))
  }

  test("Adding single part to holder and modifying the markup value generates correct line item") {
    val holder = quoteHolder
    holder.currentPart(Some(part1))
    holder.markup("15")
    holder.addLineItem(3)

    holder.lineItems should be(List(LineItem(0, Identifier(123), 3, 10.0, 0.15)))
  }

  test("Adding single part to holder and then re-adding the same part with a different quantity and markup values correctly updates the generated line items.") {
    val holder = quoteHolder
    holder.currentPart(Some(part1))
    holder.addLineItem(3)
    holder.addLineItem(2)

    holder.lineItems should be(List(LineItem(0, Identifier(123), 2, 10.0, 0.25)))
  }

  test("Adding multiple parts to the holder generates the correct line items") {
    val holder = quoteHolder
    holder.currentPart(Some(part1))
    holder.addLineItem(3)
    holder.currentPart(Some(part2))
    holder.addLineItem(2)

    holder.lineItems should be(List(LineItem(0, Identifier(123), 3, 10.0, 0.25), LineItem(1, Identifier(234), 2, 20.0, 0.25)))
  }

  test("Adding multiple parts and then modifying the quantity & markup of one correctly updates the generated lien items") {
    val holder = quoteHolder
    holder.currentPart(Some(part1))
    holder.addLineItem(3)
    holder.currentPart(Some(part2))
    holder.addLineItem(2)
    holder.currentPart(Some(part1))
    holder.addLineItem(5)

    holder.lineItems should be(List(LineItem(0, Identifier(123), 5, 10.0, 0.25), LineItem(1, Identifier(234), 2, 20.0, 0.25)))
  }

  test("Adding multiple line items, then removing the first generates the correct line item") {
    val holder = quoteHolder
    holder.currentPart(Some(part1))
    holder.addLineItem(3)
    holder.currentPart(Some(part2))
    holder.addLineItem(2)
    holder.currentPart(Some(part1))
    holder.addLineItem(0)

    holder.lineItems should be(List(LineItem(0, Identifier(234), 2, 20.0, 0.25)))
  }

  test("Adding multiple line items, then removing the first and re-adding it with a different quantity results in the line items being in the correct order") {
    val holder = quoteHolder
    holder.currentPart(Some(part1))
    holder.addLineItem(3)
    holder.currentPart(Some(part2))
    holder.addLineItem(2)
    holder.currentPart(Some(part1))
    holder.addLineItem(0)
    holder.addLineItem(4)

    holder.lineItems should be(List(LineItem(0, Identifier(234), 2, 20.0, 0.25), LineItem(1, Identifier(123), 4, 10.0, 0.25)))
  }

}