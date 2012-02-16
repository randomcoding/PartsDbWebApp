/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.search

import scala.xml.NodeSeq

/**
 * Base class for Search page with their providers.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
abstract class SearchPageProvider {

  val name: String
  val providesType: String

  def renderSearchControls: NodeSeq
}

object CustomerSearchPageProvider extends SearchPageProvider {
  override val name = "Customer"
  override val providesType = "Customer"

  override def renderSearchControls: NodeSeq = <lift:embed what="_customer_search"/>
}

/*object QuoteSearchPageProvider extends SearchPageProvider {
  override val name = "Quote"
  override val providesType = "Quote"

  override def renderSearchControls: NodeSeq = <lift:embed what="_quote_search"/>
}*/ 