/**
 *
 */
package uk.co.randomcoding.partsdb.db

import uk.co.randomcoding.partsdb.core._
import id.Identifier
import address.Address
import contact.ContactDetails
import contact.contacttype._

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

  test("Can convert Contact Type to JSON") {
    val emailContact = Email("email@example.com")
    val emailJson: String = emailContact
    emailJson should be("""{"emailAddress":"email@example.com"}""")

    val phoneContact = Phone("+44121 987 4321")
    val phoneJson: String = phoneContact
    phoneJson should be("""{"phoneNumber":"+44121 987 4321","international":false}""")

    val mobileContact = Mobile("+447653890123", true)
    val mobileJson: String = mobileContact
    mobileJson should be("""{"mobileNumber":"+447653890123","international":true}""")
  }

  test("Can convert JSON to Contact Type ") {
    val json = """{"emailAddress":"email2@example.com"}"""
    checkJsonConversion[Email](json, Email("email2@example.com"))

    // TODO Add Mobile & Phone tests
  }

  test("Can convert Contact Details to JSON") {
    pending
  }

  test("Can convert JSON to Contact Details") {
    pending
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
    pending
  }

  test("Can convert JSON to Part") {
    pending
  }
}