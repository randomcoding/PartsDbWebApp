import sbt._
import Keys._

object ProjectBuild extends Build {
	import BuildSettings._
	import ShellPrompt._
	import Dependencies._
	import com.github.siasia.WebPlugin._

	lazy val root = Project("root", 
		file("."),
		settings = buildSettings ++ Unidoc.settings ++ Seq (
                    scaladocOptions := Seq(),
                    // Disable publish and publish-local for empty root project
                    publish := {},
                    publishLocal := {}
                )
	) aggregate(coreProject, liftProject, dbProject, loggingProject)

	lazy val coreProject: Project = Project("core",
		file("core"),
		delegates = root :: Nil,
		settings = buildSettings ++ Seq(libraryDependencies ++= coreProjectDeps,
			name := "parts-db-app-core"
		)
	) dependsOn (loggingProject)

	lazy val liftProject: Project = Project("lift", 
		file("lift"),
		settings = buildSettings ++ Seq(libraryDependencies ++= liftProjectDeps,
			name := "parts-db-app-lift"
		) ++ webSettings
	) dependsOn(dbProject % "test->test;compile->compile")

	lazy val dbProject: Project = Project("db",
		file("db"),
		settings = buildSettings ++ Seq(libraryDependencies ++= dbProjectDeps,
			name := "parts-db-app-db"
		)
	) dependsOn(coreProject)

	lazy val loggingProject: Project = Project("logging",
		file("logging"),
		settings = buildSettings ++ Seq(libraryDependencies ++= loggingDeps,
			name := "parts-db-app-logging"
		)
	)

	val commonDeps = testDeps ++ jodaDeps

	val coreProjectDeps = commonDeps ++ Seq(liftMongoRecord, rogue)

	val dbProjectDeps = Seq(liftJson, liftCommon, liftMongoRecord) ++ commonDeps

	val liftProjectDeps = commonDeps ++ liftDeps ++ jettyDeps
}

