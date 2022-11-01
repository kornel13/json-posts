import Dependencies._

val projectSettings = Seq(
  Compile / mainClass := Some("posting.Main"),
  name := "json-posts",
  Compile / run / fork := true,
)

inThisBuild(
  Seq(
    version := "1.0.0",
    scalaVersion := "2.13.10",
    scalacOptions ++= Seq(
      "-language:higherKinds",
      "-deprecation",
      "-feature",
      "-Xlint:-unused,_",
    ),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
  ),
)

lazy val root = (project in file("."))
  .settings(projectSettings)
  .settings(
    libraryDependencies ++= Cats.All ++ Circe.All ++ Http4s.All ++ Prometheus.All ++ Config.All ++ Logging.All
      ++ Enumeratum.All ++ Seq(BetterFiles),
  )
  .settings(testSettings)

val testSettings = Seq(
  libraryDependencies ++= Seq(
    Testing.ScalaTest % Test,
    Testing.ScalaMock % Test,
  ),
)
