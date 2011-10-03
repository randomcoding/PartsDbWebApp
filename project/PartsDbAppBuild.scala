import sbt._
import Keys._

object PartsDbAppBuild extends Build {
	import BuildSettings._
	import ShellPrompt._
	import Dependencies._

	val projectVersion = "0.1-SNAPSHOT"

	lazy val buildProject = Project("buildProject", 
		file("."),
		settings = buildSettings
	) aggregate(coreProject, liftProject, dbProject)

	lazy val coreProject = Project("parts-db-core",
		file("core"),
		settings = buildSettings ++ Seq(libraryDependencies ++= coreProjectDeps)
	)

	lazy val liftProject = Project("lift", 
		file("lift"),
		settings = buildSettings ++ Seq(libraryDependencies ++= liftProjectDeps)
	) dependsOn(coreProject, dbProject)

	lazy val dbProject = Project("db",
		file("db"),
		settings = buildSettings ++ Seq(libraryDependencies ++= dbProjectDeps)
	) dependsOn(coreProject)

	val coreProjectDeps = Seq(logging) ++ testDeps

	val dbProjectDeps = Seq(logging) ++ testDeps ++ mongoDeps

	val liftProjectDeps = Seq(logging) ++ testDeps ++ liftDeps
}

/**
 * Common build settings for projects.
 */
object BuildSettings {
  val buildOrganization = "uk.co.randomcoding"
  val buildVersion      = "1.0-SNAPSHOT"
  val buildScalaVersion = "2.9.1"

  val buildSettings = Defaults.defaultSettings ++ Seq (
    organization := buildOrganization,
    version      := buildVersion,
    scalaVersion := buildScalaVersion,
    shellPrompt  := ShellPrompt.buildShellPrompt
  )
}

/**
 * Shell prompt which show the current project, 
 * git branch and build version
 */
object ShellPrompt {
  object devnull extends ProcessLogger {
    def info (s: => String) {}
    def error (s: => String) { }
    def buffer[T] (f: => T): T = f
  }
  def currBranch = (
    ("git status -sb" lines_! devnull headOption)
      getOrElse "-" stripPrefix "## "
  )

  val buildShellPrompt = { 
    (state: State) => {
      val currProject = Project.extract (state).currentProject.id
      "%s:%s:%s> ".format (
        currProject, currBranch, BuildSettings.buildVersion
      )
    }
  }
}

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
	val liftUtil = liftDep("lift-util", "compile")
	val liftCommon = liftDep("lift-common", "compile")
	val liftWebkit = liftDep("lift-webkit", "compile")
	val liftJson = liftDep("lift-json", "compile")

	val mongoQuery = mongoDep("casbah-query", "compile")
	val mongoCore = mongoDep("casbah-core", "compile")
	val mongoCommons = mongoDep("casbah-commons", "compile")

	val logging = "org.slf4j" % "slf4j-simple" % "1.6.1"

	val scalatest = "org.scalatest" %% "scalatest" % "1.6.1" % "test"
	
	// Dependency groups
	val testDeps = Seq(scalatest)

	val liftDeps = Seq(liftUtil, liftCommon, liftWebkit, liftJson)
	val mongoDeps = Seq(mongoQuery, mongoCore, mongoCommons)
}
