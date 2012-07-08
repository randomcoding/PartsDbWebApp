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
package uk.co.randomcoding.partsdb.lift.util

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

/**
 * Methods to work with Dates & Times
 *
 * These mainly use the `JodaTime` classes and provide an implicit conversion from a `java.util.Date` to a `org.joda.time.DateTime`
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object DateHelpers {

  /**
   * Default date format for use with to string calls.
   *
   * This will render a date as 05/12/2012 for Dec. 5th 2012
   */
  final val dateFormat = "dd/MM/yyyy"

  /**
   * Get a date as a string, using the default [[uk.co.randomcoding.partsdb.lift.util.DateHelpers#dateFormat]]
   */
  def dateString(date: DateTime) = date.toString(dateFormat)

  def date(dateString: String, format: String = dateFormat): DateTime = {
    val formatter = DateTimeFormat.forPattern(format)
    formatter.parseDateTime(dateString).toDate()
  }

  /**
   * Implicit conversion from a `java.util.Date` to a `org.joda.time.DateTime`
   */
  implicit def dateToJoda(date: java.util.Date): DateTime = new DateTime(date)

  implicit def jodaToDate(dateTime: DateTime): java.util.Date = dateTime.toDate()
}
