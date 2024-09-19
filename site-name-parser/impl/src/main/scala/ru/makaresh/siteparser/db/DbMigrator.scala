package ru.makaresh.siteparser.db

import cats.effect.kernel.{Resource, Sync}
import cats.implicits.*
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import javax.sql.DataSource

/**
 * @author Bannikov Makar
 */
class DbMigrator[F[_]: Sync](dataSource: DataSource) {

  val logger: Logger[F] = Slf4jLogger.getLogger[F]

  def migrate(): Resource[F, MigrateResult] =
    Resource.eval(
      Flyway
        .configure()
        .dataSource(dataSource)
        .load()
        .migrate()
        .pure[F]
    )
}
