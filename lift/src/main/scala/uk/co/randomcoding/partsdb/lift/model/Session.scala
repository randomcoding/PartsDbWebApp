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
package uk.co.randomcoding.partsdb.lift.model

import net.liftweb.http.SessionVar
import uk.co.randomcoding.partsdb.core.user.Role.{ NO_ROLE, Role }
import uk.co.randomcoding.partsdb.core.transaction.Payment

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object Session {
  object currentUser extends SessionVar[(String, Role)](("", NO_ROLE))

  object recentNewPayments extends SessionVar[Seq[Payment]](Nil)
}
