import sbt._
import Keys._

object PartsDbAppBuild extends Build {
	import BuildSettings._
	import ShellPrompt._
	import Dependencies._
	import com.github.siasia.WebPlugin._

	lazy val root = Project("root", 
		file("."),
		settings = buildSettings
	) aggregate(coreProject, liftProject, dbProject)

	lazy val coreProject: Project = Project("parts-db-core",
		file("core"),
		delegates = root :: Nil,
		settings = buildSettings ++ Seq(libraryDependencies ++= coreProjectDeps,
			name := "parts-db-app-core"
		)
	)

	lazy val liftProject: Project = Project("lift", 
		file("lift"),
		settings = buildSettings ++ Seq(libraryDependencies ++= liftProjectDeps,
			name := "parts-db-app-lift"
		) ++ webSettings
	) dependsOn(coreProject, dbProject)

	lazy val dbProject: Project = Project("db",
		file("db"),
		settings = buildSettings ++ Seq(libraryDependencies ++= dbProjectDeps,
			name := "parts-db-app-db"
		)
	) dependsOn(coreProject)

	val coreProjectDeps = Seq() ++ testDeps ++ loggingDeps

	val dbProjectDeps = Seq() ++ loggingDeps ++ testDeps ++ mongoDeps

	val liftProjectDeps = Seq() ++ loggingDeps ++ testDeps ++ liftDeps ++ jettyDeps
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
