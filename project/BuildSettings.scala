import sbt._
import Keys._
//import com.typesafe.sbteclipse.core.EclipsePlugin._

/**
 * Common build settings for projects.
 */
object BuildSettings {
  import Resolvers._

  val buildOrganization = "uk.co.randomcoding"
  val buildVersion      = "1.0-SNAPSHOT"
  val buildScalaVersion = "2.9.1"

  val buildSettings = Defaults.defaultSettings ++ Seq (
    organization := buildOrganization,
    version      := buildVersion,
    scalaVersion := buildScalaVersion,
    shellPrompt  := ShellPrompt.buildShellPrompt,
    scalacOptions := Seq("-deprecation", "-unchecked")
//    EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource
  )
}
