package ru.makaresh.siteparser

import cats.effect.kernel.{Async, Resource}
import cats.implicits.*
import com.comcast.ip4s.*
import com.zaxxer.hikari.HikariDataSource
import doobie.ExecutionContexts
import doobie.hikari.HikariTransactor
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.{Router, Server}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig.ConfigSource
import ru.makaresh.siteparser.config.AppConfig
import ru.makaresh.siteparser.db.DbMigrator
import ru.makaresh.siteparser.task.module.TaskModule
import ru.makaresh.siteparser.title.module.TitleModule

/**
 * @author Bannikov Makar
 */
object Server:
  def apply[F[_]: Async]: Resource[F, Server] = {

    given Logger[F] = Slf4jLogger.getLogger[F]

    val cfg        = ConfigSource.default.loadOrThrow[AppConfig]
    val dataSource = new HikariDataSource()
    dataSource.setDriverClassName(cfg.db.driver)
    dataSource.setJdbcUrl(cfg.db.url)
    dataSource.setUsername(cfg.db.user)
    dataSource.setPassword(cfg.db.password)

    val host = Hostname.fromString(cfg.api.host).getOrElse(host"localhost")
    val port = Port.fromInt(cfg.api.port).getOrElse(port"8090")

    for {
      ec         <- ExecutionContexts.fixedThreadPool[F](cfg.db.pool)
      tx          = HikariTransactor[F](dataSource, ec)
      a          <- DbMigrator[F](dataSource).migrate()
      client     <- EmberClientBuilder.default[F].build
      taskModule  = TaskModule[F](tx)
      titleModule = TitleModule[F](tx, taskModule.service, client, cfg.titleBatch)
      server     <- EmberServerBuilder
                      .default[F]
                      .withHost(host)
                      .withPort(port)
                      .withHttpApp(Router("/" -> titleModule.titleRoutes).orNotFound)
                      .build
    } yield server
  }
