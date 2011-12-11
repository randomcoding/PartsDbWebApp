/**
 *
 */
package uk.co.randomcoding.partsdb.db.util

import java.security.MessageDigest

/**
 * Provides common helper functions of a utility nature
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object Helpers {

  /**
   * Get the MD5 hash of a string as a String
   *
   * This is used to convert a plaintext password into its hashed form as stored in the database
   *
   * @param input The string to generate the hash for
   *
   * @return The MD5SUM hash of the input string
   */
  def hash(input: String): String = new String(MessageDigest.getInstance("MD5").digest(input.getBytes))
}