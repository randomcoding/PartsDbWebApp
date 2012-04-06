import sbt._
import Keys._
//import com.typesafe.sbteclipse.core.EclipsePlugin._

/**
 * Common build settings for projects.
 */
object BuildSettings {
  import Resolvers._

  val buildOrganization = "uk.co.randomcoding"
  val buildVersion      = "0.4.5-SNAPSHOT"
  val buildScalaVersion = "2.9.1"

  val buildSettings = Defaults.defaultSettings ++ Seq (
    organization := buildOrganization,
    version      := buildVersion,
    scalaVersion := buildScalaVersion,
    parallelExecution in Test := false,
    shellPrompt  := ShellPrompt.buildShellPrompt,
    scalacOptions := Seq("-deprecation", "-unchecked"),
//    EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource
    unmanagedSourceDirectories in Compile <<= (scalaSource in Compile)(Seq(_)),
    unmanagedSourceDirectories in Test <<= (scalaSource in Test)(Seq(_))
  )
}
