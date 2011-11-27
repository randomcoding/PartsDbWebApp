/**
 *
 */
package uk.co.randomcoding.partsdb.core.address

import uk.co.randomcoding.partsdb.core.id.Identifier
import uk.co.randomcoding.partsdb.core.util.CountryCodes._

/**
 * Parses address strings into [[uk.co.randomcoding.partsdb.core.address.Address]]es
 *
 * == Example
 * This will generate an [[uk.co.randomcoding.partsdb.core.address.Address]] object from the input string
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
  def unapply(addressString: String): Option[Address] = {
    val lines = addressString.split("""[,\.]+""") map (_ trim)

    identifyCountry(lines) match {
      case None => None
      case Some(code) => Some(Address(Identifier(0), lines(0), lines.mkString("\n").trim, code._2))
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
  private def identifyCountry(addressLines: Seq[String]): Option[(String, String)] = {
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