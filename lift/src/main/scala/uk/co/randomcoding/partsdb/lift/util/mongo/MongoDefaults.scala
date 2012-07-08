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
package uk.co.randomcoding.partsdb.lift.util.mongo
import uk.co.randomcoding.partsdb.db.mongo.MongoConfig

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
@deprecated("No longer in use", "0.1")
object MongoDefaults {
  val defaultDbName = "MainDb"
  val defaultCollectionName = "MainCollection"
  val defaultCollection = "Collection" //MongoConfig.getCollection(defaultDbName, defaultCollectionName)
}
