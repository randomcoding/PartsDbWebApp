/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet

import net.liftweb.http.StatefulSnippet

/**
 * Compound trait that combines `StatefulSnippet`, [[uk.co.randomcoding.partsdb.lift.util.snippet.DataValidation]] and
 * [[uk.co.randomcoding.partsdb.lift.util.snippet.ErrorDisplay]]
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait StatefulValidatingErrorDisplaySnippet extends StatefulSnippet with ErrorDisplay with DataValidation