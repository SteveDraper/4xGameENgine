name := "server"

version := "1.0"

libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.2.2"
libraryDependencies += "org.scalaz" %% "scalaz-concurrent" % "7.2.2"

resolvers += Resolver.sonatypeRepo("releases")

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.7.1")

scalaVersion := "2.11.8"

lazy val http4sVersion = "0.13.2a"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion
)