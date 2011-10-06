import sbt._
import Keys._

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

