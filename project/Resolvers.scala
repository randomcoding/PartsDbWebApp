import sbt._
import Keys._

object Resolvers {
    val snapshotsRepo = ScalaToolsSnapshots

    val snapshots = Seq(snapshotsRepo)
}
