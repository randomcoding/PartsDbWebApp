/**
 *
 */
package uk.co.randomcoding.partsdb.core.document

import net.liftweb.mongodb.record.BsonMetaRecord
import net.liftweb.mongodb.record.BsonRecord
import net.liftweb.mongodb.MongoDB
import com.mongodb.QueryBuilder
import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.record.field.LongField
import net.liftweb.mongodb.record.field.ObjectIdPk

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class DocumentId private () extends MongoRecord[DocumentId] with ObjectIdPk[DocumentId] {

  def meta = DocumentId

  object currentId extends LongField(this)

  override def equals(that: Any): Boolean = that match {
    case id: DocumentId => currentId.get == id.currentId.get
    case _ => false
  }

  override def hashCode: Int = getClass.hashCode + currentId.get.hashCode
}

object DocumentId extends DocumentId with MongoMetaRecord[DocumentId] {
  import com.foursquare.rogue.Rogue._

  def nextId() = DocumentId where (_.id exists true) get match {
    case None => DocumentId.createRecord.currentId(1).save match {
      case id: DocumentId => id
      case _ => DocumentId.createRecord.currentId(-1l)
    }
    case Some(id) => {
      id.currentId.set(id.currentId.get + 1l)
      id.save
      id
    }
  }
}