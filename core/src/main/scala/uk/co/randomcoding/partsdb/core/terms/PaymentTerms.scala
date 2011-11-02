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
sealed abstract class PaymentTerms(val days: Int) {}

object PaymentTerms {
  /**
   * Pattern match types of terms based on days
   */
  def unapply(paymentDays: Int): Option[PaymentTerms] = paymentDays match {
    case 30 => Some(ThirtyDays)
    case 60 => Some(SixtyDays)
    case 90 => Some(NinetyDays)
    case other => Some(CustomTerms(other))
  }
}

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

/**
 * Custom payment terms
 */
case class CustomTerms(paymentDays: Int) extends PaymentTerms(paymentDays)