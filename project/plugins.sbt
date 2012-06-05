
resolvers += Classpaths.typesafeResolver

resolvers += "Sonatype releases" at "http://oss.sonatype.org/content/repositories/releases"

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse" % "1.5.0")
//addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.0.0")

resolvers += "Web plugin repo" at "http://siasia.github.com/maven2"    

libraryDependencies <+= sbtVersion(v => v match {
case "0.11.0" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.0-0.2.8"
case "0.11.1" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.1-0.2.10"
case "0.11.2" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.2-0.2.11"
case "0.11.3" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.3-0.2.11.1"
})

// idea plugin
//resolvers += "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"

//addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.0.0")
