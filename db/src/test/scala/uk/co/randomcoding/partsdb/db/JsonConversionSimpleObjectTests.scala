/**
 *
 */
package uk.co.randomcoding.partsdb.db

import uk.co.randomcoding.partsdb.core.id.Identifier

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
class JsonConversionSimpleObjectTests extends LiftJsonTests {

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
    fail("Not Implemented Yet")
  }

  test("Can convert JSON to Address") {
    fail("Not Implemented Yet")
  }

  test("Can convert Customer to JSON") {
    fail("Not Implemented Yet")
  }

  test("Can convert JSON to Customer") {
    fail("Not Implemented Yet")
  }

  test("Can convert Supplier to JSON") {
    fail("Not Implemented Yet")
  }

  test("Can convert JSON to Supplier") {
    fail("Not Implemented Yet")
  }

  test("Can convert Part to JSON") {
    fail("Not Implemented Yet")
  }

  test("Can convert JSON to Part") {
    fail("Not Implemented Yet")
  }
}