/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util

import org.joda.time.DateTime

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
  val dateFormat = "dd/MM/yyyy"

  /**
   * Get a date as a string, using the default [[uk.co.randomcoding.partsdb.lift.util.DateHelpers#dateFormat]]
   */
  def dateString(date: DateTime) = date.toString(dateFormat)

  /**
   * Implicit conversion from a `java.util.Date` to a `org.joda.time.DateTime`
   */
  implicit def dateToJoda(date: java.util.Date): DateTime = new DateTime(date)
}