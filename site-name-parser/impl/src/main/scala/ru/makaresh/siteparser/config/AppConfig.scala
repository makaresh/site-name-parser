package ru.makaresh.siteparser.config

import cats.effect.kernel.Async
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import pureconfig.ConfigReader
import pureconfig.generic.derivation.default.*

case class AppConfig(api: ApiConfig, db: DbConfig, titleBatch: Int) derives ConfigReader

case class ApiConfig(
  host: String,
  port: Int
) derives ConfigReader

case class DbConfig(
  host: String,
  port: Int,
  user: String,
  password: String,
  databaseName: String,
  driver: String,
  pool: Int
) derives ConfigReader:
  def url: String = s"jdbc:postgresql://$host:$port/$databaseName"

class SiteParserTransactor[F[_]: Async](config: DbConfig) {
  val transactor = for {
    ec <- ExecutionContexts.fixedThreadPool(config.pool)
    tx <- HikariTransactor.newHikariTransactor(
            config.driver,
            config.url,
            config.user,
            config.password,
            ec
          )
  } yield tx
}
