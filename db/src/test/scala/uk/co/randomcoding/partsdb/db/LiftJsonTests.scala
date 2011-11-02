/**
 *
 */
package uk.co.randomcoding.partsdb.db

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite

import uk.co.randomcoding.partsdb.core.id.Identifier

import net.liftweb.json.Serialization.write
import net.liftweb.json.{ parseOpt, parse }
import net.liftweb.json.DefaultFormats

/**
 * Provides the base testing capabilities for all testing of converting between JSON and objects and vice versa
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
trait LiftJsonTests extends FunSuite with ShouldMatchers {
  implicit val formats = DefaultFormats

  /**
   * Convert an object to its JSON representation
   */
  implicit def toJsonString(obj: AnyRef): String = write(obj)

  /**
   * Attempt to convert a JSON string into an object of a given type
   */
  def fromJsonString[T <: AnyRef](json: String)(implicit mf: Manifest[T]): T = parseOpt(json) match {
    case None => fail("Failed to convert %s to a %s".format(json, mf))
    case Some(jObj) => jObj.extract[T]
  }

  /**
   * Test check method to call for the `convert JSON to ...` tests.
   *
   * This performs the conversion of the JSON into an object and then compares it with `expected`
   */
  def checkJsonConversion[T <: AnyRef](json: String, expected: T)(implicit mf: Manifest[T]): Unit = fromJsonString(json) should be(expected)

}