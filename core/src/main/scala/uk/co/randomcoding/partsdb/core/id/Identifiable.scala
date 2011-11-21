/**
 *
 */
package uk.co.randomcoding.partsdb.core.id

/**
 * Marks an object as being identifiable (within the Database).
 *
 * This is used to get the object's identifier to ensure that it is unique in the database and that the correct item is accessed each time.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait Identifiable {
  /**
   * The name of the case class parameter that maps to the object's [[uk.co.randomcoding.partsdb.code.id.Identifier]]
   *
   * This should take the general form of `classNameId` where `className` is the appropriately camel cased version of the enclosing class's name
   */
  val identifierFieldName: String

  /**
   * Accessor for the actual identifier value to allow building of the MongoDB query to find the item
   *
   * This default implementation uses Java reflection to get the method declared with the name given by `identifierFieldName`
   * and get the `id` value of the returned [[uk.co.randomcoding.partsdb.core.id.Identifier]]
   *
   * It is not intended that this be overridden, but can be if required.
   */
  def id: Long = getClass.getMethod(identifierFieldName).invoke(this).asInstanceOf[Identifier].id
}