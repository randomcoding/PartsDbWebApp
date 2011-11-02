/**
 *
 */
package uk.co.randomcoding.partsdb.core.id

import net.liftweb.common.Logger
import uk.co.randomcoding.partsdb.core.document.DocumentType._
import uk.co.randomcoding.partsdb.core.document._

/**
 * Base class for identifier types.
 *
 * ***This Should really use a mthod to get a new unique id number I think***
 *
 * @constructor Create a new (hopefully) unique identifier
 * @param uniqueId A long that should be unique amongst all ids
 * @param identifierType A string denoting the type of identifier this is. Can be used by case classes to indicate sub types of identifiers. This defaults to an empty string for non document type objects.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
case class Identifier(id: Long)

object Identifier {
  implicit def longToIdentifier(id: Long): Identifier = Identifier(id)
}