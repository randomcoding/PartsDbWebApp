/*
 * Copyright (C) 2012 RandomCoder <randomcoder@randomcoding.co.uk>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *    RandomCoder - initial API and implementation and/or initial documentation
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
