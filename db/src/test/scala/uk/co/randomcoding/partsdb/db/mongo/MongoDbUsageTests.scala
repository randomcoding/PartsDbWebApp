/*
 * Copyright (c) 2012 RandomCoder <randomcoder@randomcoding.co.uk>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    RandomCoder - initial API and implementation and/or initial documentation
 */
package uk.co.randomcoding.partsdb.db.mongo

import uk.co.randomcoding.partsdb.core.address.Address
import com.foursquare.rogue.Rogue._
import uk.co.randomcoding.partsdb.core.user.{Role, User}

/**
 * Tests for general behaviour of mongo db
 *
 * These tests use the main MongoDB and Rogue APIs to test their behaviour
 * in certain cases so that when implemented we can be certain of the behaviour
 * of these two APIs.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class MongoDbUsageTests extends MongoDbTestBase {
  override val dbName = "mongoDbUsageTests"

  test("Adding exactly same entity twice does not cause errors or corrupt the entity record") {
    val entity = Address.create("Test Address", "Test Address", "United Kingdom")

    val saved1 = entity.save
    val saved2 = entity.save

    saved1 should be theSameInstanceAs (saved2)

    val saved3 = entity.saveTheRecord()
    val saved4 = entity.saveTheRecord()

    saved3.get should be theSameInstanceAs (saved4.get)
  }

  test("Adding entity of the same type but with different with the same object id as a previously entered entity - replaces the original entity in the database") {
    val entity1 = Address.create("Test Address 1", "Test Address 1", "United Kingdom")
    val saved1 = entity1.save
    val id = saved1.id.get
    val entity2 = Address.create("Test Address 2", "Test Address 2", "United Kingdom").id(id)

    val saved2 = entity2.save

    Address where (_.shortName eqs "Test Address 1") get() should be('empty)

    saved2 should equal(entity2)

    (Address where (_.id eqs id) get).get should (equal(entity2) and equal(saved2))
  }

  test("Adding entity of a different type with the same object id as a previously entered entity adds both entities to their own database") {
    val entity1 = Address.create("Test Address", "Test Address", "United Kingdom")
    val saved1 = entity1.save
    val id = saved1.id.get
    val entity2 = User.createRecord.username("User").password("password").role(Role.USER).id(id)

    val saved2 = entity2.save

    Address where (_.id eqs id) get() should (equal(Some(entity1)) and equal(Some(saved1)))

    User where (_.id eqs id) get() should (equal(Some(entity2)) and equal(Some(saved2)))
  }

  test("Adding entity of a different type with the same object id as a previously entered entity type which share the same database replaces the first entity with the second") {
    // As there are no entity types that share an underlying database yet this is not a relevant test at present.
    pending
  }


}
