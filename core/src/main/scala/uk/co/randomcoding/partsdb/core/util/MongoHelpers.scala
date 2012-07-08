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
package uk.co.randomcoding.partsdb.core.util

import org.bson.types.ObjectId

/**
 * Helper functions and implicit conversions to make Mongo DB a little easier to use with the PDWA API
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object MongoHelpers {

  /**
   * Convert a String to a BSON `ObjectId`. If the String is not a valid `ObjectId` this implicit conversion will fail at runtime
   */
  implicit def stringToObjectId(oid: String): ObjectId = new ObjectId(oid)
}
