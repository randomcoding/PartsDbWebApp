/**
 *
 */
package uk.co.randomcoding.partsdb.core.terms

/**
 * Defines payment terms in days
 *
 * @constructor Creates a new instance of some payment terms
 * @param days The number of days available to pay
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
case class PaymentTerms(val days: Int) {}