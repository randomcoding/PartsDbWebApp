
resolvers += Classpaths.typesafeResolver

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse" % "1.4.0")

resolvers += "Web plugin repo" at "http://siasia.github.com/maven2"    

addSbtPlugin("com.github.siasia" %% "xsbt-web-plugin" % "0.1.2")

