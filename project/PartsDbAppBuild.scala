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

