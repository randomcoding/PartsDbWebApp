/**
 *
 */
package uk.co.randomcoding.partsdb.core.id

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait Identifiable {
  val identifierFieldName: String

  def id: Long
}