/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import uk.co.randomcoding.partsdb.core.contact.ContactDetails._
import uk.co.randomcoding.partsdb.core.contact.ContactDetails
import com.foursquare.rogue.Rogue._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class ContactDetailsRecordTest extends MongoDbTestBase {
  override val dbName = "ContactDetailsRecordTest"

  test("Add new Contact Details") {
    val contacts = add("Dave", "01234 567890", "", "")

    val expected = createRecord.contactName("Dave").phoneNumber("01234 567890")

    contacts should be(Some(expected))

    findNamed("Dave") should be(List(expected))
    findById(contacts.get.id.get) should be(Some(expected))
  }

  test("Adding the same ContactDetails more than once does not result in a duplication, or a change of underlying id") {
    val contacts = add("Dave", "01234 567890", "", "")
    val contactsId = contacts.get.id.get
    contacts should be('defined)
    add("Dave", "01234 567890", "", "") should be(contacts)
    add("Dave", "01234 567890", "", "").get.id.get should be(contactsId)

    findNamed("Dave")(0).id.get should be(contactsId)

    findById(contactsId) should be(contacts)
  }

  test("Find Matching can find similar records by object id") {
    val contacts = add("Dave", "01234 567890", "", "")

    val otherContacts = createRecord.id(contacts.get.id.get).contactName("Sally").mobileNumber("7867868745")

    findMatching(otherContacts) should be(contacts)
  }

  test("Find Matching can find similar records by contact name and one contact detail") {
    val dave = add("Dave", "01234 567890", "", "")
    val otherDave = createRecord.contactName("Dave").phoneNumber("01234 567890").emailAddress("em@ai.l").mobileNumber("07777888999")
    findMatching(otherDave) should be(dave)
    val dave1 = add("Dave1", "", "07777888999", "")
    val otherDave1 = createRecord.contactName("Dave1").phoneNumber("01234 567890").emailAddress("em@ai.l").mobileNumber("07777888999")
    findMatching(otherDave1) should be(dave1)
    val dave2 = add("Dave2", "", "", "em@ai.l")
    val otherDave2 = createRecord.contactName("Dave2").phoneNumber("01234 567890").emailAddress("em@ai.l").mobileNumber("07777888999")
    findMatching(otherDave2) should be(dave2)
  }

  test("Adding contact details with the same contact name and at least one contact number the same returns the original record") {
    val dave = add("Dave", "01234 567890", "", "")
    val otherDave = createRecord.contactName("Dave").phoneNumber("01234 567890").emailAddress("em@ai.l").mobileNumber("07777888999")

    add(otherDave) should be(dave)
  }

  test("Removing a Contact Details that exists in the database") {
    val contacts = add("Dave", "01234 567890", "", "")
    val contactsId = contacts.get.id.get
    val contacts2 = add("Sally", "01234 678901", "", "")
    contacts2 should be('defined)

    remove(contactsId)

    findById(contactsId) should be(None)
    findNamed("Dave") should be(Nil)
    (ContactDetails where (_.id exists true) fetch) should be(List(contacts2.get))
  }

  test("Removing a Contact Details from an empty database does not cause an error or exception") {
    import org.bson.types.ObjectId

    remove(new ObjectId)
  }

  test("Removing a Contact Details that does not exist from a populated database does not cause an error or exception") {
    import org.bson.types.ObjectId

    add("Dave", "01234 567890", "", "") should be('defined)
    add("Sally", "01234 678901", "", "") should be('defined)

    remove(new ObjectId)
  }

  test("Find a Contact Details that is present in the database") {
    val contacts1 = add("Dave", "01234 567890", "", "").get
    val contacts2 = add("Sally", "01234 678901", "", "").get
    // Find by oid, & name
    findById(contacts1.id.get) should be(Some(contacts1))
    findById(contacts2.id.get) should be(Some(contacts2))

    findNamed("Dave") should be(List(contacts1))
    findNamed("Sally") should be(List(contacts2))
  }

  test("Find a Contact Details that is not present in the database") {
    import org.bson.types.ObjectId
    add("Dave", "01234 567890", "", "")
    add("Sally", "01234 678901", "", "")
    // Find by oid & name
    findNamed("Garry") should be(Nil)
    findById(new ObjectId) should be(None)
  }

  test("Modify a Contact Details") {
    val contacts1 = add("Dave", "01234 567890", "", "").get
    val contactId = contacts1.id.get
    modify(contactId, "Fred", "", "07777888999", "an@em.ail")

    val expected = ContactDetails.createRecord.contactName("Fred").mobileNumber("07777888999").emailAddress("an@em.ail")
    findNamed("Dave") should be(Nil)
    findNamed("Fred") should be(List(expected))
  }

  test("Modify a Contact Details does not modify its object id") {
    val contacts1 = add("Dave", "01234 567890", "", "").get
    val contactId = contacts1.id.get
    modify(contactId, "Fred", "", "07777888999", "an@em.ail")

    val expected = ContactDetails.createRecord.contactName("Fred").mobileNumber("07777888999").emailAddress("an@em.ail")

    findById(contactId) should be(Some(expected))
  }

  test("Equality and HashCode") {
    val contact1 = createRecord contactName "Dave" phoneNumber "123" mobileNumber "456" emailAddress "one@two.three"
    val contact2 = createRecord contactName "Dave" phoneNumber "123" mobileNumber "456" emailAddress "one@two.three"
    val contact3 = createRecord contactName "Dave" phoneNumber "123" mobileNumber "456" emailAddress "one@two.three"

    contact1 should (be(contact2) and be(contact3))
    contact2 should (be(contact1) and be(contact3))
    contact3 should (be(contact1) and be(contact2))

    contact1.hashCode should (be(contact2.hashCode) and be(contact3.hashCode))
  }

}