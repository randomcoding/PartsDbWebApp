/**
 *
 */
package uk.co.randomcoding.partsdb.core.terms

/**
 * @constructor
 * @param days The number of days available to pay
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
case class PaymentTerms(val days: Int)

/**
 * Thirty day terms
 */
case object ThirtyDays extends PaymentTerms(30)

/**
 * Sixty day terms
 */
case object SixtyDays extends PaymentTerms(60)

/**
 * Ninety day terms
 */
case object NinetyDays extends PaymentTerms(90)

