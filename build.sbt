scalaVersion := "2.13.3"

scalacOptions := Seq("-deprecation")

lazy val doobieVersion = "0.9.0"
lazy val http4sVersion = "0.21.6"
lazy val circeVersion = "0.13.0"
lazy val scalaTestVersion = "3.1.0"
lazy val scalaMockVersion = "4.4.0"

libraryDependencies ++= Seq(
  // Doobie dependencies
  "org.tpolecat" %% "doobie-core"      % doobieVersion,
  "org.tpolecat" %% "doobie-postgres"  % doobieVersion,
  "org.tpolecat" %% "doobie-scalatest" % doobieVersion % "test",

  // Http4s dependencies
  "org.http4s" %% "http4s-dsl"  % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,

  // Circe
  "io.circe"  %% "circe-generic"  % circeVersion,
  "io.circe"  %% "circe-config"   % "0.7.0",
  "io.circe"  %% "circe-literal"  % circeVersion,
  "io.circe"  %% "circe-optics"   % circeVersion  % "it",
  "io.circe"  %% "circe-parser"   % circeVersion,

  // Scalatest
  "org.scalatest" %% "scalatest"  % scalaTestVersion % "it, test", 
  "org.scalamock" %% "scalamock"  % scalaMockVersion % "test"

)

resolvers ++= Seq(
  "Type safe repository" at "https://repo.typesafe.come/typesafe/releases/"
)
