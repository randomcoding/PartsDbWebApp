/*
 * Copyright (C) 2012 RandomCoder <randomcoder@randomcoding.co.uk>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *    RandomCoder - initial API and implementation and/or initial documentation
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

  def nextId() = DocumentId get match {
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
