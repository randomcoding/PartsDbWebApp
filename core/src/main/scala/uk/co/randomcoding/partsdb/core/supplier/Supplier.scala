/**
 *
 */
package uk.co.randomcoding.partsdb.core.supplier

import uk.co.randomcoding.partsdb.core.contact.ContactDetails
import uk.co.randomcoding.partsdb.core.part.PartCost
import net.liftweb.mongodb.record.{ MongoRecord, MongoMetaRecord }
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._
import uk.co.randomcoding.partsdb.core.address.Address

/**
 * Supplier information including contact details and a free form notes
 *
 * @author Jane Rowe
 * @author RandomCoder - Changed to MongoRecord class
 *
 */
class Supplier private () extends MongoRecord[Supplier] with ObjectIdPk[Supplier] {
  def meta = Supplier

  /**
   * The name of the supplier
   */
  object supplierName extends StringField(this, 50)

  /**
   * The contact details for this supplier
   */
  object contactDetails extends BsonRecordField(this, ContactDetails)

  /**
   * The parts this supplier provides as [[uk.co.randomcoding.partsdb.core.part.PartCost]] objects
   */
  object suppliedParts extends BsonRecordListField(this, PartCost)

  /**
   * The address of the supplier
   */
  object businessAddress extends ObjectIdRefField(this, Address)

  /**
   * Any notes about this supplier.
   */
  object notes extends StringField(this, 500)

  /**
   * A `Supplier` is equal to another `Supplier` if their name, contact details and business address fields match
   */
  override def equals(that: Any): Boolean = that match {
    case other: Supplier => supplierName.get == other.supplierName.get &&
      contactDetails.get == other.contactDetails.get &&
      businessAddress.get == other.businessAddress.get /*&&
      suppliedParts.get.toSet == other.suppliedParts.get.toSet*/
    case _ => false
  }

  private val hashCodeFields = Seq(supplierName, contactDetails, suppliedParts)

  override def hashCode: Int = getClass.hashCode + (hashCodeFields map (_.get.hashCode) sum)
}

object Supplier extends Supplier with MongoMetaRecord[Supplier] {

  import org.bson.types.ObjectId
  import com.foursquare.rogue.Rogue._

  /**
   * Create a new `Supplier` record but '''does not''' save it to the database
   */
  def create(name: String, contacts: ContactDetails, businessAddress: Address, partsSupplied: Seq[PartCost]): Supplier = {
    Supplier.createRecord.supplierName(name).contactDetails(contacts).businessAddress(businessAddress.id.get).suppliedParts(partsSupplied.toList)
  }

  /**
   * Add a new supplier to the database
   *
   * If there is a matching record present, this will be returned and the addition will not happen.
   * If there is no matching record then a new record will be created and returned.
   *
   * @return An `Option[Supplier]` that is populated if the addition succeeded, or found a match, and `None` if the save operation failed.
   */
  def add(supplier: Supplier): Option[Supplier] = findMatching(supplier) match {
    case Some(s) => Some(s)
    case _ => supplier.save match {
      case s: Supplier => Some(s)
      case _ => None
    }
  }

  /**
   * Add a new supplier to the database
   *
   * If there is a matching record present, this will be returned and the addition will not happen.
   * If there is no matching record then a new record will be created and returned.
   *
   * @return An `Option[Supplier]` that is populated if the addition succeeded, or found a match, and `None` if the save operation failed.
   */
  def add(name: String, contacts: ContactDetails, businessAddress: Address, suppliedParts: Seq[PartCost]): Option[Supplier] = {
    add(create(name, contacts, businessAddress, suppliedParts))
  }

  /**
   * Find a supplier that matches the provided one.
   *
   * A match is made on:
   *   - Object Id
   *   - name and (contact details or address)
   *
   * @return An `Option` containing the match if found or `None` otherwise
   */
  def findMatching(supplier: Supplier): Option[Supplier] = findById(supplier.id.get) match {
    case Some(s) => Some(s)
    case _ => Supplier.where(_.supplierName eqs supplier.supplierName.get).or(
      _.where(_.contactDetails eqs supplier.contactDetails.get),
      _.where(_.businessAddress eqs supplier.businessAddress.get)).get
  }

  /**
   * Find all suppliers that have the given name
   */
  def findNamed(name: String): List[Supplier] = Supplier where (_.supplierName eqs name) fetch

  def findById(oid: ObjectId): Option[Supplier] = Supplier where (_.id eqs oid) get

  def remove(oid: ObjectId): Option[Boolean] = findById(oid) match {
    case Some(s) => Some(s.delete_!)
    case _ => None
  }

  /**
   * Updates the record with the given `Object Id` with the new `name`, `contact details` and `supplied parts` values.
   *
   * Any updates that are required to the contact details or the supplied parts themselves must be done externally to this method
   */
  def modify(oid: ObjectId, newName: String, newContacts: ContactDetails, newAddress: Address, newParts: Seq[PartCost], newNotes: String) = {
    Supplier.where(_.id eqs oid).modify(_.supplierName setTo newName).
      and(_.contactDetails setTo newContacts).
      and(_.suppliedParts setTo newParts).
      and(_.businessAddress setTo newAddress.id.get).
      and(_.notes setTo newNotes) updateMulti
  }
}
