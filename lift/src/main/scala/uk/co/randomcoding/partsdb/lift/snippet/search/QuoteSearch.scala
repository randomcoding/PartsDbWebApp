/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet.search

import uk.co.randomcoding.partsdb.lift.util.TransformHelpers.styledAjaxText
import uk.co.randomcoding.partsdb.lift.util.CustomerDisplay
import net.liftweb.util.Helpers._
import net.liftweb.http.SHtml._
import net.liftweb.http.js.{ JsCmds, JsCmd }
import net.liftweb.util.Helpers._
import uk.co.randomcoding.partsdb.core.document.Document
import uk.co.randomcoding.partsdb.lift.util.QuoteDisplay
//import uk.co.randomcoding.partsdb.db.search.QuoteSearchProvider

/**
 * @todo: Should this be stateful - refactor as this is a bit mixed up
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object QuoteSearch extends SearchSnippet {
  /*  val searchKeys = List(quotePartName)

  var partName = ""

  def termForKey(key: String): MongoSearchTerm = {
    key match {
      case SearchKeys.quotePartName => regexTerm(key, partName)
    }
  }
  def render = {
    //var partName = ""

    "#partNameEntry" #> styledAjaxText(partName, (s: String) => updateValue(() => partName = s)(s)) &
      "#results" #> QuoteDisplay.displayTable(Nil) //getMatching[Document](("documentId" $exists true) ++ MongoDBObject("documentType" -> "QUO")) filter (doc => doc.documentType == "QUO"))
  }

  */
  /**
   * Generate the new live search results and display then in the relevant section of the page
   *
   * This gets the search terms, performs the search and displays the results in the `results` div of the main page
   */ /*
  def updateResults(s: String = "") = {
    val results = searchTerms match {
      case Nil => getMatching[Document](MongoDBObject("documentType" -> "QUO"))
      case terms => QuoteSearchProvider(collection).find(searchTerms.toSet)
      case _ => List.empty[Document]
    }

    JsCmds.SetHtml("results", QuoteDisplay.displayTable(results.sortBy(_.documentNumber)))
  }*/
}