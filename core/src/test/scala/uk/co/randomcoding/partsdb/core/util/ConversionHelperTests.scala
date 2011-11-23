/**
 *
 */
package uk.co.randomcoding.partsdb.core.util

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.id.Identifier

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class ConversionHelperTests extends FunSuite with ShouldMatchers {

  import ConversionHelpers._

  test("Implicit Conversion of valid address text generates correct address") {
    val inputText = """4 Simple Road,
      A Village,
      Near A Town,
      NR45 7JD
      UK"""

    val address: Address = inputText

    address should be(Address(Identifier(0), "4 Simple Road", inputText, "UK"))
  }
}