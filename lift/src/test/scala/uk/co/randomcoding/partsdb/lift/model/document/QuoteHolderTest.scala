/**
 *
 */
package uk.co.randomcoding.partsdb.lift.model.document

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import uk.co.randomcoding.partsdb.core.contact.ContactDetails
import uk.co.randomcoding.partsdb.core.part.{ Part, PartCost }
import uk.co.randomcoding.partsdb.core.id.Identifier
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle
import uk.co.randomcoding.partsdb.core.document.LineItem
import uk.co.randomcoding.partsdb.db.DbAccess
import uk.co.randomcoding.partsdb.db.mongo.MongoDbTestBase
import uk.co.randomcoding.partsdb.core.supplier.Supplier
import org.joda.time.DateTime

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class QuoteHolderTest extends MongoDbTestBase with ShouldMatchers {

  override val dbName = "QuoteHolderTest"

  /*lazy val databaseName = dbName
  lazy val collName = collectionName

  private lazy val access = new DbAccess {
    override val dbName = databaseName
    override val collectionName = collName
  }
  
  // TODO: Add Joda Time dependency

  private def quoteHolder = new QuoteHolder(access)

  private val vehicle = Vehicle(Identifier(345), "Vehicle 1")
  private val part1 = Part(Identifier(123), "Part 1", Some(vehicle), Some("Mod1"))
  private val part2 = Part(Identifier(234), "Part 2", Some(vehicle), Some("Mod2"))

  // adding supplier information for db to get parts prices etc.
  val lastSuppliedDate = new DateTime("2012-01-10T12:00:00").toDate

  private val part1Supplier1Cost = PartCost(Identifier(1000), part1, 10.00, lastSuppliedDate)
  private val part2Supplier1Cost = PartCost(Identifier(1001), part2, 9.00, lastSuppliedDate)
  private val part2Supplier2Cost = PartCost(Identifier(1002), part2, 11.00, lastSuppliedDate)

  val supplier1 = Supplier(Identifier(100), "Supplier 1", ContactDetails("Supplier 1"), Some(List(part1Supplier1Cost, part2Supplier1Cost)))
  val supplier2 = Supplier(Identifier(101), "Supplier 2", ContactDetails("Supplier 2"), Some(List(part2Supplier2Cost)))

  test("Empty Quote Holder generates correct empty line items list") {
    quoteHolder.lineItems should be(Nil)
  }

  test("Adding a line item with zero quantity to an empty quote holder generates an empty line items list") {
    val holder = quoteHolder
    populateDatabase()
    holder currentPart Some(part1)
    holder quantity 0
    holder.addLineItem()

    holder.lineItems should be(Nil)
  }

  test("Adding a line item with negative quantity to an empty quote holder generates an empty line items list") {
    val holder = quoteHolder
    populateDatabase()
    holder currentPart Some(part1)
    holder quantity -100
    holder.addLineItem()

    holder.lineItems should be(Nil)
  }

  test("Adding single part to holder generates correct line item") {
    val holder = quoteHolder
    populateDatabase()
    holder currentPart Some(part1)
    holder quantity 3
    holder supplier Some(supplier1)
    holder.addLineItem()

    holder.lineItems should be(List(LineItem(0, Identifier(123), 3, 10.0, 0.25)))
  }

  test("Adding single part to holder and modifying the markup value generates correct line item") {
    val holder = quoteHolder
    populateDatabase()
    holder currentPart Some(part1)
    holder markup "15"
    holder quantity 3
    holder supplier Some(supplier1)
    holder.addLineItem()

    holder.lineItems should be(List(LineItem(0, Identifier(123), 3, 10.0, 0.15)))
  }

  test("Adding single part to holder and then re-adding the same part with a different quantity and markup values correctly updates the generated line items.") {
    val holder = quoteHolder
    populateDatabase()
    holder currentPart Some(part1)
    holder quantity 3
    holder supplier Some(supplier1)
    holder.addLineItem()
    holder quantity 2
    holder.addLineItem()

    holder.lineItems should be(List(LineItem(0, Identifier(123), 2, 10.0, 0.25)))
  }

  test("Adding multiple parts to the holder generates the correct line items") {
    val holder = quoteHolder
    populateDatabase()
    holder currentPart Some(part1)
    holder quantity 3
    holder supplier Some(supplier1)
    holder.addLineItem()
    holder currentPart Some(part2)
    holder quantity 2
    holder supplier Some(supplier2)
    holder.addLineItem()

    holder.lineItems should be(List(LineItem(0, Identifier(123), 3, 10.0, 0.25), LineItem(1, Identifier(234), 2, 11.0, 0.25)))
  }

  test("Adding multiple parts and then modifying the quantity & markup of one correctly updates the generated lien items") {
    val holder = quoteHolder
    populateDatabase()
    holder currentPart Some(part1)
    holder quantity 3
    holder supplier Some(supplier1)
    holder.addLineItem()

    holder currentPart Some(part2)
    holder quantity 2
    holder supplier Some(supplier2)
    holder.addLineItem()

    holder currentPart Some(part1)
    holder quantity 5
    holder supplier Some(supplier1)
    holder.addLineItem()

    holder.lineItems should be(List(LineItem(0, Identifier(123), 5, 10.0, 0.25), LineItem(1, Identifier(234), 2, 11.0, 0.25)))
  }

  test("Adding multiple line items, then removing the first generates the correct line item") {
    val holder = quoteHolder
    populateDatabase()
    holder currentPart Some(part1)
    holder quantity 3
    holder supplier Some(supplier1)
    holder.addLineItem()

    holder currentPart Some(part2)
    holder quantity 2
    holder supplier Some(supplier1)
    holder.addLineItem()

    holder currentPart Some(part1)
    holder quantity 0
    holder supplier Some(supplier1)
    holder.addLineItem()

    holder.lineItems should be(List(LineItem(0, Identifier(234), 2, 9.0, 0.25)))
  }

  test("Adding multiple line items, then removing the first and re-adding it with a different quantity results in the line items being in the correct order") {
    val holder = quoteHolder
    populateDatabase()
    holder currentPart Some(part1)
    holder quantity 3
    holder supplier Some(supplier1)
    holder.addLineItem()

    holder currentPart Some(part2)
    holder quantity 2
    holder supplier Some(supplier2)
    holder.addLineItem()

    holder currentPart Some(part1)
    holder quantity 0
    holder.addLineItem()

    holder quantity 4
    holder supplier Some(supplier1)
    holder.addLineItem()

    holder.lineItems should be(List(LineItem(0, Identifier(234), 2, 11.0, 0.25), LineItem(1, Identifier(123), 4, 10.0, 0.25)))
  }

  test("Setting the part correctly sets the suppliers") {
    val holder = quoteHolder
    populateDatabase()
    holder currentPart Some(part1)
    holder.suppliers should be(List(("Supplier 1", Some(supplier1))))

    holder currentPart Some(part2)
    holder.suppliers should be(List(("Supplier 1", Some(supplier1)), ("Supplier 2", Some(supplier2))))
  }

  test("Setting current part and supplier means the correct base cost display value is generated") {
    val holder = quoteHolder
    populateDatabase()
    holder.currentPart(Some(part1))
    holder supplier Some(supplier1)

    val expectedCost = 10.00
    holder.currentPartBaseCostDisplay.get should be("£%.2f".format(expectedCost))

    holder.currentPart(Some(part2))
    holder supplier Some(supplier1)
    holder.currentPartBaseCostDisplay.get should be("£9.00")

    holder.currentPart(Some(part2))
    holder supplier Some(supplier2)
    holder.currentPartBaseCostDisplay.get should be("£11.00")
  }

  test("Adding a line item sets the correct total values") {
    val holder = quoteHolder
    populateDatabase()
    holder currentPart Some(part1)
    holder quantity 1
    holder supplier Some(supplier1)
    holder.addLineItem()

    val expectedPartCost = 10.00

    val expectedSubTotal = expectedPartCost + (expectedPartCost * (holder.DEFAULT_MARKUP / 100.0))
    holder.subTotal.get should be("£%.2f".format(expectedSubTotal))

    val expectedTax = expectedSubTotal * holder.taxRate.get
    holder.vatAmount.get should be("£%.2f".format(expectedTax))

    holder.totalCost.get should be("£%.2f".format(expectedSubTotal + expectedTax))
  }

  private def populateDatabase(): Unit = {
    access add part1Supplier1Cost should be(true)
    access add part2Supplier1Cost should be(true)
    access add part2Supplier2Cost should be(true)
    access add part1 should be(true)
    access add part2 should be(true)
    access add vehicle should be(true)
    access add supplier1 should be(true)
    access add supplier2 should be(true)
  }*/
}