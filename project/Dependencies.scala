import sbt.*

object Dependencies {

  object Version {
    val catsEffectVersion = "3.5.4"
    val http4sVersion     = "0.23.28"
    val scraperVersion    = "3.1.1"
    val circeVersion      = "0.14.9"
    val doobieVersion     = "1.0.0-RC6"
    val flywayVersion     = "10.18.0"
    val h2Version         = "2.3.232"
    val pureConfigVersion = "0.17.7"
    val logbackVersion    = "1.5.6"
    val scalaTestVersion  = "3.2.18"
    val scalaCheckVersion = "1.18.1"
  }

  import Version.*

  private val catsEffect          = "org.typelevel"         %% "cats-effect"                % catsEffectVersion
  private val http4sClient        = "org.http4s"            %% "http4s-ember-client"        % http4sVersion
  private val http4sServer        = "org.http4s"            %% "http4s-ember-server"        % http4sVersion
  private val http4sCirce         = "org.http4s"            %% "http4s-circe"               % http4sVersion
  private val http4sDsl           = "org.http4s"            %% "http4s-dsl"                 % http4sVersion
  private val circeCore           = "io.circe"              %% "circe-core"                 % circeVersion
  private val circeGeneric        = "io.circe"              %% "circe-generic"              % circeVersion
  private val circeParser         = "io.circe"              %% "circe-parser"               % circeVersion
  private val doobie              = "org.tpolecat"          %% "doobie-core"                % doobieVersion
  private val doobieHikari        = "org.tpolecat"          %% "doobie-hikari"              % doobieVersion
  private val postgresDoobie      = "org.tpolecat"          %% "doobie-postgres"            % doobieVersion
  private val doobiePostgresCirce = "org.tpolecat"          %% "doobie-postgres-circe"      % doobieVersion
  private val flyway              = "org.flywaydb"           % "flyway-database-postgresql" % flywayVersion
  private val h2                  = "com.h2database"         % "h2"                         % h2Version
  private val pureconfig          = "com.github.pureconfig" %% "pureconfig-core"            % pureConfigVersion
  private val logback             = "ch.qos.logback"         % "logback-classic"            % logbackVersion
  private val scalaTest           = "org.scalatest"         %% "scalatest"                  % scalaTestVersion
  private val scalaCheck          = "org.scalacheck"        %% "scalacheck"                 % scalaCheckVersion

  val testDependencies: Seq[ModuleID] = Seq(
    scalaTest  % Test,
    scalaCheck % Test,
    h2         % Test
  )

  val apiDependencies: Seq[ModuleID] = Seq(circeCore)

  val implDependencies: Seq[ModuleID] = Seq(
    catsEffect,
    http4sClient,
    http4sServer,
    http4sDsl,
    http4sCirce,
    circeGeneric,
    circeParser,
    doobie,
    doobieHikari,
    postgresDoobie,
    doobiePostgresCirce,
    flyway,
    pureconfig,
    logback
  ) ++ testDependencies
}
