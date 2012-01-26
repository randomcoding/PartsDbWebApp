/**
 *
 */
package uk.co.randomcoding.partsdb.db.search

import uk.co.randomcoding.partsdb.db.mongo.MongoDbTestBase
import uk.co.randomcoding.partsdb.db.DbAccess
import uk.co.randomcoding.partsdb.db.mongo.MongoUpdateAccess
import uk.co.randomcoding.partsdb.db.search.SearchTerm._
import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.id.Identifier

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class MongoAddressSearchProviderTest extends MongoDbTestBase {
  /*val dbName = "MongoDbSearchProviderTest"

  lazy val access = new MongoUpdateAccess() {
    override val collection = mongo
  }*/

  test("Search against an empty db returns no results") {
    fail("DB Access has changed")
    /*    val provider = AddressSearchProvider(mongo)

    provider find (Set.empty[StringSearchTerm]) should be('empty)
    provider find (Set(StringSearchTerm("addressText", exists))) should be('empty)
    provider find (Set(StringSearchTerm("addressText", doesNotExist))) should be('empty)
    provider find (Set(StringSearchTerm("addressText", "UK"))) should be('empty)*/
  }

  test("Search for Address with single search term returns correct results with single entry in database") {
    fail("DB Access has changed")
    /*val provider = AddressSearchProvider(mongo)

    val address1 = Address.createRecord.shortName("Addr").addressText("Addr Long").country("UK")

    access add address1 should be(true)

    val term = StringSearchTerm("shortName", "Addr")

    provider find term should be(List(address1))*/
  }

  test("Search for Address with single search term that does not match returns correct results with single entry in database") {
    fail("DB Access has changed")
    /*val provider = AddressSearchProvider(mongo)

    val address1 = Address(Identifier(0), "Addr", "Addr Long", "UK")

    access add address1 should be(true)

    val term = StringSearchTerm("shortName", "Addr1")

    provider find term should be('empty)*/
  }

  test("Search for Address with single search term that matchs single result returns correct results with multiple entries in database") {
    fail("DB Access has changed")
    /*val provider = AddressSearchProvider(mongo)

    val address1 = Address(Identifier(0), "Addr1", "Addr Long", "UK")
    val address2 = Address(Identifier(1), "Addr2", "Addr2 Long", "UK")
    val address3 = Address(Identifier(2), "Addr3", "Addr3 Long", "UK")

    access add address1 should be(true)
    access add address2 should be(true)
    access add address3 should be(true)

    val term = StringSearchTerm("shortName", "Addr1")

    provider.find(Set(term)) should be(List(address1))*/
  }

  test("Search for Address with single search term that matchs multiple results returns correct results with multiple entries in database") {
    fail("DB Access has changed")
    /*val provider = AddressSearchProvider(mongo)

    val address1 = Address(Identifier(0), "Addr1", "Addr Long", "UK")
    val address2 = Address(Identifier(1), "Addr2", "Addr2 Long", "UK")
    val address3 = Address(Identifier(2), "Addr3", "Addr3 Long", "UK")
    val address4 = Address(Identifier(3), "Addr4", "Addr4 Long", "US")

    access add address1 should be(true)
    access add address2 should be(true)
    access add address3 should be(true)
    access add address4 should be(true)

    val term = StringSearchTerm("country", "UK")

    provider find Set(term) should (have size (3) and
      contain(address1) and
      contain(address2) and
      contain(address3))*/
  }

  test("Search for specific Address using multiple search terms when it is only entry in database") {
    fail("DB Access has changed")
    /*val provider = AddressSearchProvider(mongo)
    val address1 = Address(Identifier(0), "Addr1", "Addr Long", "UK")
    access add address1 should be(true)

    provider find Set(StringSearchTerm("shortName", "Addr1"), StringSearchTerm("country", "UK")) should be(List(address1))*/
  }

  test("Search for specific Address using multiple search terms when there are multiple partial matches in the database") {
    fail("DB Access has changed")
    /*val provider = AddressSearchProvider(mongo)
    val address1 = Address(Identifier(0), "Addr1", "Addr Long", "UK")
    val address2 = Address(Identifier(1), "Addr2", "Addr Long", "UK")
    val address3 = Address(Identifier(2), "Addr1", "Addr2 Long", "UK")
    val address4 = Address(Identifier(3), "Addr1", "Addr Long", "US")
    access add address1 should be(true)
    access add address2 should be(true)
    access add address3 should be(true)
    access add address4 should be(true)

    provider find Set(StringSearchTerm("shortName", "Addr1"), StringSearchTerm("country", "UK")) should (have size (2) and
      contain(address1) and
      contain(address3))*/
  }

  test("Search with multiple terms for specific Address that should return no matches when there are multiple partial matches in the database") {
    fail("DB Access has changed")
    /*val provider = AddressSearchProvider(mongo)
    val address1 = Address(Identifier(0), "Addr1", "Addr Long", "UK")
    val address2 = Address(Identifier(1), "Addr2", "Addr Long", "UK")
    val address3 = Address(Identifier(2), "Addr1", "Addr2 Long", "UK")
    val address4 = Address(Identifier(3), "Addr1", "Addr Long", "US")
    access add address1 should be(true)
    access add address2 should be(true)
    access add address3 should be(true)
    access add address4 should be(true)

    provider find Set(StringSearchTerm("shortName", "Addr1"), StringSearchTerm("country", "UR")) should be('empty)*/
  }

}