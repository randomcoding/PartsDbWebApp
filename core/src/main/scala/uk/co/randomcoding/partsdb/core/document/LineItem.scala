/**
 *
 */
package uk.co.randomcoding.partsdb.core.document
import uk.co.randomcoding.partsdb.core.id.Identifier

/**
 * A line item for documents.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
case class LineItem(lineNumber: Int, partId: Identifier, quantity: Int, unitPrice: Double)