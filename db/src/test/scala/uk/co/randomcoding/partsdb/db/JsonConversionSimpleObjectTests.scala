/**
 *
 */
package uk.co.randomcoding.partsdb.db

import uk.co.randomcoding.partsdb.core._
import id.Identifier
import address.Address
import part.Part
import contact._
import customer.Customer
import terms._
import document.LineItem

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
    val emailJson = """{"emailAddress":"email2@example.com"}"""
    checkJsonConversion[Email](emailJson, Email("email2@example.com"))

    val mobileJson = """{"mobileNumber":"+447557345890","international":false}"""
    checkJsonConversion[Mobile](mobileJson, Mobile("+447557345890", false))

    val phoneJson = """{"phoneNumber":"+1115558934","international":true}"""
    checkJsonConversion[Phone](phoneJson, Phone("+1115558934", true))
  }

  test("Can convert Contact Details to JSON") {
    val contactDetails = ContactDetails("Person 1", phoneNumbers = Some(List(Phone("+44121345678"))), emailAddresses = Some(List(Email("person@somewhere.com"))))
    val json: String = contactDetails
    json should be("""{"contactName":"Person 1","phoneNumbers":[{"phoneNumber":"+44121345678","international":false}],"emailAddresses":[{"emailAddress":"person@somewhere.com"}]}""")
  }

  test("Can convert JSON to Contact Details") {
    val json = """{"contactName":"Person 1","phoneNumbers":[{"phoneNumber":"+44121345678","international":false}],"emailAddresses":[{"emailAddress":"person@somewhere.com"}]}"""
    checkJsonConversion[ContactDetails](json, ContactDetails("Person 1", phoneNumbers = Some(List(Phone("+44121345678"))), emailAddresses = Some(List(Email("person@somewhere.com")))))
  }

  test("Can convert Customer to JSON") {
    val customerJson: String = Customer(Identifier(9753), "A Customer", Identifier(4567), PaymentTerms(30), ContactDetails("A Person", phoneNumbers = Some(List(Phone("+44 543 5678 9832")))))
    val expectedJson = """{"customerId":{"id":9753},"customerName":"A Customer","billingAddress":{"id":4567},""" +
      """"terms":{"days":30},"contactDetails":{"contactName":"A Person","phoneNumbers":[{"phoneNumber":"+44 543 5678 9832","international":false}]}}"""

    customerJson should be(expectedJson)
  }

  test("Can convert JSON to Customer") {
    val json = """{"customerId":{"id":9753},"customerName":"A Customer","billingAddress":{"id":4567},""" +
      ""","terms":{"days":30},"contactDetails":{"contactName":"A Person","phoneNumbers":[{"phoneNumber":"+44 543 5678 9832","international":false}]}}"""
    val customer = Customer(Identifier(9753), "A Customer", Identifier(4567), PaymentTerms(30), ContactDetails("A Person", phoneNumbers = Some(List(Phone("+44 543 5678 9832")))))

    checkJsonConversion[Customer](json, customer)
  }

  test("Can convert Line Item to JSON") {
    val itemJson: String = LineItem(1, Identifier(234), 3, 4.50)

    itemJson should be("""{"lineNumber":1,"partId":{"id":234},"quantity":3,"unitPrice":4.5}""")
  }

  test("Can convert JSON to Line Item") {
    val itemJson = """{"lineNumber":4,"partId":{"id":654},"quantity":1,"unitPrice":105.23}"""
    val lineItem = LineItem(4, Identifier(654), 1, 105.23)
    checkJsonConversion[LineItem](itemJson, lineItem)
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