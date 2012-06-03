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
package uk.co.randomcoding.partsdb.core.system

import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.record.field._
import com.foursquare.rogue.Rogue._

/**
 * Holder for system wide data.
 *
 * This class is '''not''' intended to be used directly.
 * All field access should be through the companion object's methods.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class SystemData private () extends MongoRecord[SystemData] with ObjectIdPk[SystemData] {
  override val meta = SystemData

  /**
   * The version of the currently running database.
   *
   * This is an integer to allow the sequential application of database transformations
   * if migrating through multiple versions.
   *
   * Default value is 1.
   */
  private object databaseVersion extends IntField(this) {
    override val defaultValue = 1
  }

  /**
   * The vat rate in use.
   *
   * Defaults to 0.2 (20%)
   */
  private object vatRate extends DoubleField(this) {
    override val defaultValue = 0.20
  }

  /**
   * The path on the local user's machine that contains the PDF files for vehicles
   *
   * defaults to `C:/Documents and Settings/All Users/Vehicle PDF Data/`
   */
  private object vehiclePdfPath extends StringField(this, 255) {
    override val defaultValue = "C:/Documents and Settings/All Users/Vehicle PDF Data"
  }
}

/**
 * Accessor object for System data info.
 *
 * Provides methods for getting and setting the various system data fields.
 */
object SystemData extends SystemData with MongoMetaRecord[SystemData] {
  private def dataRecord = SystemData.get() match {
    case Some(sd) => sd
    case _ => SystemData.createRecord.save
  }

  def databaseVersion = dataRecord.databaseVersion.get

  def databaseVersion_=(version: Int): Unit = SystemData.where(_.id exists true).modify(_.databaseVersion setTo version).updateMulti

  def vatRate = dataRecord.vatRate.get

  def vatRate_=(vat: Double): Unit = SystemData.where(_.id exists true).modify(_.vatRate setTo vat).updateMulti

  def vehiclePdfPath = dataRecord.vehiclePdfPath.get

  def vehiclePdfPath_=(pdfPath: String): Unit = SystemData.where(_.id exists true).modify(_.vehiclePdfPath setTo pdfPath).updateMulti
}