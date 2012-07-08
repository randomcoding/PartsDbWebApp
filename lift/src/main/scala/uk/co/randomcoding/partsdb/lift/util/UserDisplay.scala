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
package uk.co.randomcoding.partsdb.lift.util

import scala.xml.NodeSeq

import uk.co.randomcoding.partsdb.core.user.User
import uk.co.randomcoding.partsdb.lift.util._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object UserDisplay extends TabularEntityDisplay {
  type EntityType = User

  override val rowHeadings = List("User Name", "User Role")

  /**
   * Display the `<td>` elements for a user
   */
  override def displayEntity(userDetails: User, editLink: Boolean, displayLink: Boolean): NodeSeq = {
    <td>{ userDetails.username.get }</td>
    <td>{ userDetails.role.get }</td> ++
      editAndDisplayCells("User", userDetails.id.get, editLink, displayLink)
  }
}
