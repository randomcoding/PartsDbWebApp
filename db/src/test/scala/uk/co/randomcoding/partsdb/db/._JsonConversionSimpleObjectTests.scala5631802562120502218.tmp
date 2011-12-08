/**
 *
 */
package uk.co.randomcoding.partsdb.db

import uk.co.randomcoding.partsdb.core.id.Identifier
import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.part.Part

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
class JsonConversionSimpleObjectTests extends JsonConversionTesting {

  test("Can convert JSON to Identifier") {
    val json = """{ "id" : 1234 }"""

    checkJsonConversion[Identifier](json, Identifier(1234))
  }

  test("Can convert Identifier to JSON") {
    val id = Identifier(5432)

    val json: String = id

    json should be("""{"id":5432}""")

    checkJsonConversion[Identifier](json, Identifier(5432))
  }

  test("Can convert Address to JSON") {
    val address = Address(Identifier(4567), "Addr", "Addr Long", "UK")
    val json: String = address

    json should be("""{"addressId":{"id":4567},"shortName":"Addr","addressText":"Addr Long","country":"UK"}""")
    checkJsonConversion[Address](json, Address(Identifier(4567), "Addr", "Addr Long", "UK"))
  }

  test("Can convert JSON to Address") {
    val json = """{ "addressId" : {"id":4567},
      "shortName" : "Addr",
      "addressText" : "Addr Long",
      "country" : "UK" }"""

    checkJsonConversion[Address](json, Address(Identifier(4567), "Addr", "Addr Long", "UK"))
  }

  test("Can convert Customer to JSON") {
    pending
  }

  test("Can convert JSON to Customer") {
    pending
  }

  test("Can convert Supplier to JSON") {
    pending
  }

  test("Can convert JSON to Supplier") {
    pending
  }

  test("Can convert Part to JSON") {
    val part = Part(Identifier(4568), "MyPart", 1.51)
    val json: String = part

    json should be("""{"partId":{"id":4568},"partName":"MyPart","partCost":1.51}""")
    checkJsonConversion[Part](json, Part(Identifier(4568), "MyPart", 1.51))
  }

  test("Can convert JSON to Part") {
    val json = """{ "partId" : {"id":4568},
      "partName" : "MyPart",
      "partCost" : 1.51 }"""

    checkJsonConversion[Part](json, Part(Identifier(4568), "MyPart", 1.51))
  }
}