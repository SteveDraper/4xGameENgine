name := "server"

version := "1.0"



resolvers += Resolver.sonatypeRepo("releases")
resolvers += Resolver.bintrayRepo("oncue", "releases")

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.7.1")


scalaVersion := "2.11.8"

lazy val http4sVersion = "0.11.3"

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % "7.1.6",
  "org.scalaz" %% "scalaz-concurrent" % "7.1.6",
  "io.argonaut" %% "argonaut" % "6.1",
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "org.http4s" %% "http4s-argonaut" % http4sVersion,
  "oncue.knobs" %% "core" % "3.3.3"
)

mainClass in (Compile,run) := Some("server.ServerApp")