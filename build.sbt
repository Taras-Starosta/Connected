name := "Connected"

version := "0.1"

scalaVersion := "2.13.8"

lazy val AkkaVersion = "2.6.18"
lazy val AkkaGroup = "com.typesafe.akka"
def akkaDep(module: String): ModuleID = AkkaGroup %% s"akka-$module" % AkkaVersion
def tapirDep(module: String): ModuleID = "com.softwaremill.sttp.tapir" %% s"tapir-$module" % "0.20.0-M9"
def jwtDep(module: String): ModuleID = "com.github.jwt-scala" %% s"jwt-$module" % "9.0.3"
def doobieDep(module: String): ModuleID = "org.tpolecat" %% s"doobie-$module" % "1.0.0-RC1"
def scribeDep(module: String = ""): ModuleID = "com.outr" %% s"scribe${
  if(module.isBlank) "" else s"-$module"
}" % "3.6.10"
def cacheDep(module: String): ModuleID = "com.github.cb372" %% s"scalacache-$module" % "1.0.0-M6"

libraryDependencies ++= Seq(
  akkaDep("actor-typed"),
  akkaDep("stream-typed"),
  AkkaGroup %% "akka-http" % "10.2.7",
  tapirDep("akka-http-server"),
  tapirDep("swagger-ui-bundle"),
  tapirDep("asyncapi-docs"),
  tapirDep("asyncapi-circe-yaml"),
  jwtDep("core"),
  jwtDep("upickle"),
  "com.lihaoyi" %% "upickle" % "1.4.3",
  doobieDep("core"),
  doobieDep("hikari"),
  doobieDep("postgres"),
  "com.github.pureconfig" %% "pureconfig" % "0.17.1",
  scribeDep(),
  scribeDep("file"),
  scribeDep("slf4j"),
  "com.github.dwickern" %% "scala-nameof" % "4.0.0" % "provided",
  "com.github.t3hnar" %% "scala-bcrypt" % "4.3.0",
  "com.github.daddykotex" %% "courier" % "3.1.0",
  "org.flywaydb" % "flyway-core" % "8.4.3",
  "org.webjars" % "swagger-ui" % "4.4.1-1",
  cacheDep("core"),
  cacheDep("caffeine"),
)

assembly / assemblyJarName := "connected.jar"

ThisBuild / assemblyMergeStrategy := {
  case PathList("META-INF", "maven", "org.webjars", "swagger-ui", "pom.properties") =>
    MergeStrategy.first
  case PathList("META-INF", "maven", "org.webjars", "swagger-ui", "pom.xml") =>
    MergeStrategy.first
  case x =>
    val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
    oldStrategy(x)
}

scalacOptions ++= Seq(
  "-deprecation"
)