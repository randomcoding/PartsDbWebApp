/**
 *
 */
package uk.co.randomcoding.partsdb.core.id

/**
 * BAse class for identifier types.
 *
 * ***This Should really use a mthod to get a new unique id number I think***
 *
 * @constructor Create a new (hopefully) unique identifier
 * @param uniqueId A long that should be unique amongst all ids
 * @param identifierType A string denoting the type of identifier this is. Can be used by case classes to indicate sub types of identifiers. This defaults to an empty string for non document type objects.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
abstract class Identifier(val uniqueId: Long, val identifierType: String = "") {
    override def toString: String = "Identifier: %s%d".format(identifierType, uniqueId)

    override def hashCode: Int = getClass.hashCode + identifierType.hashCode + uniqueId.hashCode

    override def equals(that: Any): Boolean = {
        that.isInstanceOf[Identifier] match {
            case true => {
                val other = that.asInstanceOf[Identifier]
                uniqueId == other.uniqueId && identifierType == other.identifierType
            }
            case false => false
        }
    }
}