name := "api"

version := "1.0"

libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.2.2"
libraryDependencies += "org.scalaz" %% "scalaz-concurrent" % "7.2.2"
libraryDependencies += "io.argonaut" %% "argonaut" % "6.1"

resolvers += Resolver.sonatypeRepo("releases")

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.7.1")

scalaVersion := "2.11.8"
