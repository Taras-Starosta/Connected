name := "Connected"

version := "0.1"

scalaVersion := "2.13.8"

val AkkaVersion = "2.6.18"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % "10.2.7",
  "com.softwaremill.sttp.tapir" %% "tapir-akka-http-server" % "0.20.0-M6",
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % "0.20.0-M6",
  "com.github.jwt-scala" %% "jwt-core" % "9.0.3",
  "com.github.jwt-scala" %% "jwt-upickle" % "9.0.3",
  "com.lihaoyi" %% "upickle" % "1.4.3",
  "org.tpolecat" %% "doobie-core" % "1.0.0-RC1",
  "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC1",
  "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC1",
  "com.github.pureconfig" %% "pureconfig" % "0.17.1",
  "com.outr" %% "scribe" % "3.6.10",
  "com.outr" %% "scribe-file" % "3.6.10",
  "com.outr" %% "scribe-slf4j" % "3.6.10",
)