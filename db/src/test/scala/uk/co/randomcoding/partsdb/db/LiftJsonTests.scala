/**
 *
 */
package uk.co.randomcoding.partsdb.db

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import net.liftweb.json._
import net.liftweb.json.Serialization._
import uk.co.randomcoding.partsdb.core.address.AddressId

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
class LiftJsonTests extends FunSuite with ShouldMatchers {
  private implicit val formats = DefaultFormats

  test("Can convert JSON to AddressId") {
    val json = """{ "id" : 1234 }"""

    parseOpt(json) match {
      case None => fail("No Match!")
      case Some(jObj) => jObj.extract[AddressId] should be(AddressId(1234))
    }
  }

  test("Can convert AddressId to JSON") {
    val id = AddressId(5432)

    val json: String = write(id)

    json should be("""{"id":5432}""")

    parse(json).extract[AddressId] should be(AddressId(5432))
  }
}