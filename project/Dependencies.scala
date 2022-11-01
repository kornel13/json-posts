import sbt._

object Dependencies {

  object Cats {
    private val Version = "3.3.11"

    lazy val Effect = "org.typelevel" %% "cats-effect" % Version

    lazy val All: Seq[ModuleID] = Seq(Effect)
  }

  object Circe {
    private val Version = "0.14.1"

    lazy val Core    = "io.circe" %% "circe-core"           % Version
    lazy val Generic = "io.circe" %% "circe-generic"        % Version
    lazy val Extras  = "io.circe" %% "circe-generic-extras" % Version
    lazy val Parser  = "io.circe" %% "circe-parser"         % Version

    lazy val All: Seq[ModuleID] = Seq(Core, Generic, Extras, Parser)
  }

  object Http4s {
    private val Version = "0.23.11"

    lazy val DSL    = "org.http4s" %% "http4s-dsl"          % Version
    lazy val Client = "org.http4s" %% "http4s-blaze-client" % Version
    lazy val Server = "org.http4s" %% "http4s-blaze-server" % Version
    lazy val Circe  = "org.http4s" %% "http4s-circe"        % Version

    lazy val Core = "org.http4s" %% "http4s-core" % Version

    lazy val All: Seq[ModuleID] = Seq(Server, Client, DSL, Circe)
  }

  object Fs2 {
    lazy val Core = "co.fs2" %% "fs2-core" % "3.2.5"

    lazy val All: Seq[ModuleID] = Seq(Core)
  }

  object Enumeratum {
    private val enumeratumCirceVersion = "1.7.0"

    lazy val Enumeratum = "com.beachape" %% "enumeratum"       % enumeratumCirceVersion
    lazy val CirceEnum  = "com.beachape" %% "enumeratum-circe" % enumeratumCirceVersion

    lazy val All: Seq[ModuleID] = Seq(Enumeratum, CirceEnum)
  }

  object Config {
    private val Version = "0.17.1"
    lazy val PureConfig = "com.github.pureconfig" %% "pureconfig" % Version
    lazy val Enumeratum = "com.github.pureconfig" %% "pureconfig-enumeratum" % Version
    lazy val CatsConfig = "com.github.pureconfig" %% "pureconfig-cats" % Version

    lazy val All: Seq[ModuleID] = Seq(PureConfig, Enumeratum, CatsConfig)
  }

  object Logging {
    lazy val ScalaLogging = "com.typesafe.scala-logging" %% "scala-logging"  % "3.9.4" // logging API
    lazy val Logback      = "ch.qos.logback"             % "logback-classic" % "1.2.11" // logging impl
    lazy val Log4Cats     = "org.typelevel"              %% "log4cats-slf4j" % "2.3.1"
    lazy val Janino       = "org.codehaus.janino"        % "janino"          % "3.0.7"

    lazy val All: Seq[ModuleID] = Seq(ScalaLogging, Logback, Log4Cats, Janino)
  }

  object Prometheus {
    private val Version = "0.5.0-M2"

    lazy val Epimetheus = "io.chrisdavenport" %% "epimetheus"          % Version
    lazy val Logback    = "io.prometheus"     % "simpleclient_logback" % "0.6.0"

    lazy val All: Seq[ModuleID] = Seq(Epimetheus, Logback)
  }

  object Testing {
    lazy val ScalaTest = "org.scalatest" %% "scalatest" % "3.2.9"
    lazy val ScalaMock = "org.scalamock" %% "scalamock" % "4.4.0"
  }

  lazy val BetterFiles = "com.github.pathikrit" %% "better-files" % "3.9.1"
}
