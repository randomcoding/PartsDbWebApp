/**
 *
 */
package uk.co.randomcoding.partsdb.core.address

import uk.co.randomcoding.partsdb.core.util.CountryCodes._
import uk.co.randomcoding.partsdb.core.util.CountryCode
import uk.co.randomcoding.partsdb.core.util.CountryCode._

/**
 * Parses address strings into [[uk.co.randomcoding.partsdb.core.address.Address]]es
 *
 * == Example ==
 *
 * This will generate an [[uk.co.randomcoding.partsdb.core.address.Address]] object from the input string
 *
 * {{{
 * val addressText = "4 House Lane, A Village, A Town, DG6 9GH. UK"
 * val address = addressText match {
 *   case AddressParser(a) => a
 *   case _ => NullAddress
 * }
 * }}}
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object AddressParser {
  /**
   * Pattern matching on an input tuple of (short name, address lines, country) that attempts to create an
   * [[uk.co.randomcoding.partsdb.core.address.Address]] from the input.
   *
   * If the first element of the tuple is empty then the first line of the address lines is used for the address's short name
   * The address lines have any trailing commas of full stops removed and are then `trimmed`. Empty lines are then removed.
   * Then the country is matched to a country code in [[uk.co.randomcoding.partsdb.core.util.CountryCodes]].
   *
   * If there is a country code match (either on the country code or full name) and the address lines are not basically empty,
   * then an [[uk.co.randomcoding.partsdb.core.address.Address]] is generated and wrapped in an `Option`
   *
   * @return An Option[[[uk.co.randomcoding.partsdb.core.address.Address]]] if the input string matches
   */
  def unapply(nameAndAddress: (String, Seq[String], String)): Option[Address] = {
    nameAndAddress match {
      case (_, Nil, _) => None
      case _ => {
        val addressLines = nameAndAddress._2 map (_.replaceAll("""[,\.]$""", "") trim) filter (_ nonEmpty) //addressString.split("""[,\.]+""") map (_ trim)
        val shortName = nameAndAddress._1.trim match {
          case "" => addressLines(0)
          case s => s
        }

        identifyCountry(nameAndAddress._3) match {
          case Some(code) if (addressLines.size >= 1) => Some(Address(shortName, addressLines.mkString("\n"), code.countryName))
          case _ => None
        }
      }
    }
  }

  /**
   * Gets the country code from the address.
   *
   * Currently this will check for a UK Post Code and return UK, otherwise will return the last line of the input list
   *
   * This is the key method as it is used by the match in apply to determine if an address is valid.
   *
   * This needs a better means of country identification
   */
  private def identifyCountry(country: String): Option[CountryCode] = matchToCountryCode(country)

  /**
   * Performs shortcut matching on addresses.
   *
   * This allows an address's country to be identified without it being explicitly stated in the address text
   * as is likely to be the case for UK addresses.
   *
   * Currently identifies a UK Post Code and returns `Some("UK")`
   *
   * @return An `Option[String]` with the country's code is a quick match was possible, otherwise returns `None`
   */
  /*private def quickCountryMatch(addressLines: Seq[String]): Option[String] = {
    var quickMatch: Option[String] = None
    if (hasPostCode(addressLines)) quickMatch = Some("UK")

    quickMatch
  }

  private val postCodeRegex = """[A-Z]{1,2}[1-9][0-9]? [0-9][A-Z]{2}""".r
  private val zipCodeRegex = """[0-9]{5,6}""".r

  private val zipCodeOption = (addressLine: String) => zipCodeRegex.findFirstIn(addressLine)

  private val postCodeOption = (addressLine: String) => postCodeRegex.findFirstIn(addressLine)

  private val isCode = (line: String, codeMatchOption: (String => Option[String])) => codeMatchOption(line) isDefined

  private val hasPostCode = (addressLines: Seq[String]) => addressLines.find(isCode(_, postCodeOption)).isDefined*/
}
