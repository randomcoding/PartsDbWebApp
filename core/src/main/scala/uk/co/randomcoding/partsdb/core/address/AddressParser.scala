/**
 *
 */
package uk.co.randomcoding.partsdb.core.address

import uk.co.randomcoding.partsdb.core.id.DefaultIdentifier
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
 *   case AddressParser(addr) => addr
 *   case _ => NullAddress
 * }
 * }}}
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object AddressParser {
  /**
   * Pattern matching on an address string
   *
   * The input text is split into lines by commas or full stops and then the Country code is extracted from
   * the address lines and matched to a country code in [[uk.co.randomcoding.core.util.CountryCodes]].
   * If there is a match (either on the country code or full name) then an [[uk.co.randomcoding.partsdb.core.address.Address]]
   * is generated with a  and using the first line of the address
   * as the `shortName` parameter. The address text is generated from the split input string (no trailing punctuation)
   *
   * @return An Option[[[uk.co.randomcoding.partsdb.core.address.Address]]] if the input string matches
   */
  def unapply(nameAndAddress: (String, String)): Option[Address] = {
    val addressString = nameAndAddress._2
    val lines = addressString.split("""[,\.]+""") map (_ trim)
    val shortName = nameAndAddress._1.trim match {
      case "" => lines(0)
      case s => s
    }

    identifyCountry(lines) match {
      case Some(code) if (lines.size > 1) => Some(Address.createRecord.shortName(shortName).addressText(lines.mkString("\n")).country(code.countryName))
      case _ => None
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
  private def identifyCountry(addressLines: Seq[String]): Option[CountryCode] = {
    quickCountryMatch(addressLines) match {
      case Some(code) => matchToCountryCode(code)
      case None => countryCodeFromAddressLines(addressLines)
    }
  }

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
  private def quickCountryMatch(addressLines: Seq[String]): Option[String] = {
    var quickMatch: Option[String] = None
    if (hasPostCode(addressLines)) quickMatch = Some("UK")

    quickMatch
  }

  private val postCodeRegex = """[A-Z]{1,2}[1-9][0-9]? [0-9][A-Z]{2}""".r
  private val zipCodeRegex = """[0-9]{5,6}""".r

  private val zipCodeOption = (addressLine: String) => zipCodeRegex.findFirstIn(addressLine)

  private val postCodeOption = (addressLine: String) => postCodeRegex.findFirstIn(addressLine)

  private val isCode = (line: String, codeMatchOption: (String => Option[String])) => codeMatchOption(line) isDefined

  private val hasPostCode = (addressLines: Seq[String]) => addressLines.find(isCode(_, postCodeOption)).isDefined
}