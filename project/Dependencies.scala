import sbt._

/**
 * Declaration of dependencies, library versions etc.
 */
object Dependencies {
	// Common Versions for libraries
	val mongoVersion = "2.1.5-1"
	val liftVersion = "2.4-M4"
	
	// Functions to create dependencies
	val liftDep = (componentId: String, scope: String ) => "net.liftweb" %% componentId % liftVersion % scope
	val mongoDep = (componentId: String, scope: String)	=> "com.mongodb.casbah" %% componentId % mongoVersion % scope
	
	// Actual dependencies
	// liftweb
	val liftUtil = liftDep("lift-util", "compile")
	val liftCommon = liftDep("lift-common", "compile")
	val liftWebkit = liftDep("lift-webkit", "compile")
	val liftJson = liftDep("lift-json", "compile")

	// mongodb - casbah
	val mongoQuery = mongoDep("casbah-query", "compile")
	val mongoCore = mongoDep("casbah-core", "compile")
	val mongoCommons = mongoDep("casbah-commons", "compile")

	// jetty
	val jetty7 = "org.eclipse.jetty" % "jetty-webapp" % "7.3.0.v20110203" % "jetty"
	val logback = "ch.qos.logback" % "logback-classic" % "0.9.26"
	
	// logging
	val grizzled = "org.clapper" %% "grizzled-slf4j" % "0.6.6"
	val logging = "org.slf4j" % "slf4j-simple" % "1.6.1"

	val scalatest = "org.scalatest" %% "scalatest" % "1.6.1" % "test"
	
	// Dependency groups
	val testDeps = Seq(scalatest)
	val liftDeps = Seq(liftUtil, liftCommon, liftWebkit, liftJson)
	val mongoDeps = Seq(mongoQuery, mongoCore, mongoCommons)
	val loggingDeps = Seq(grizzled, logging)
	val jettyDeps = Seq(jetty7)
}
