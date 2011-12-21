/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.search
import uk.co.randomcoding.partsdb.db.search.MongoSearchProvider
import scala.xml.NodeSeq
import uk.co.randomcoding.partsdb.db.search.CustomerSearchProvider
import uk.co.randomcoding.partsdb.lift.util.mongo.MongoDefaults

/**
 * Base class for Search page with their providers.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
abstract class SearchPageProvider {
  val searchProvider: MongoSearchProvider

  def renderSearchControls: NodeSeq
}

object CustomerSearchPageProvider extends SearchPageProvider {
  override val searchProvider = CustomerSearchProvider(MongoDefaults.defaultCollection)

  override def renderSearchControls: NodeSeq = <lift:embed what="_customer_search"/>
}