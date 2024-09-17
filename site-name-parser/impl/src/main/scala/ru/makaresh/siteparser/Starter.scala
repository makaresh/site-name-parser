package ru.makaresh.siteparser

import cats.effect.*
import com.comcast.ip4s.*
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig.ConfigSource
import ru.makaresh.siteparser.config.{AppConfig, SiteParserTransactor}
import ru.makaresh.siteparser.task.module.TaskModule
import ru.makaresh.siteparser.title.module.TitleModule

object Starter extends IOApp {

  given Logger[IO] = Slf4jLogger.getLogger[IO]

  override def run(args: List[String]): IO[ExitCode] = {
    val cfg = ConfigSource.default.loadOrThrow[AppConfig]

    val server = for {
      tx         <- SiteParserTransactor[IO](cfg.db).transactor
      httpClient <- EmberClientBuilder.default[IO].build
      taskModule  = TaskModule[IO](tx)
      titleModule = TitleModule[IO](tx, taskModule.service, httpClient, cfg.titleBatch)
      api         = Router("/" -> titleModule.titleRoutes).orNotFound
      server     <- EmberServerBuilder
                      .default[IO]
                      .withHost(ipv4"0.0.0.0")
                      .withPort(port"8090")
                      .withHttpApp(api)
                      .build
    } yield server

    server
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }
}
