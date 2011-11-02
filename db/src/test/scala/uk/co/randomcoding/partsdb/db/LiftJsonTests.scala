/**
 *
 */
package uk.co.randomcoding.partsdb.db

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import net.liftweb.json.Serialization._
import net.liftweb.json._
import uk.co.randomcoding.partsdb.core.id._

/**
 * This test class ''should'' contain a test to convert to and from all the different types that are stored in the database and their JSON forms
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
class LiftJsonTests extends FunSuite with ShouldMatchers {
  private implicit val formats = DefaultFormats

  test("Can convert JSON to Identifier") {
    val json = """{ "id" : 1234 }"""

    parseOpt(json) match {
      case None => fail("No Match!")
      case Some(jObj) => jObj.extract[Identifier] should be(Identifier(1234))
    }
  }

  test("Can convert Identifier to JSON") {
    val id = Identifier(5432)

    val json: String = write(id)

    json should be("""{"id":5432}""")

    parse(json).extract[Identifier] should be(Identifier(5432))
  }

  test("Can convert Address to JSON") {
    fail("Not Implemented Yet")
  }

  test("Can convert JSON to Address") {
    fail("Not Implemented Yet")
  }

}