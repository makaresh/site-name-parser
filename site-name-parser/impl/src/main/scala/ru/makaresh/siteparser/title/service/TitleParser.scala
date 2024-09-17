package ru.makaresh.siteparser.title.service

import cats.effect.kernel.Async
import cats.implicits.*
import org.http4s.client.Client
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import ru.makaresh.siteparser.api.title.Result
import ru.makaresh.siteparser.api.title.response.SiteParserError
import ru.makaresh.siteparser.title.model.Title

import java.time.Instant
import java.util.UUID
import scala.util.Try

trait TitleParser[F[_]: Async] {

  def httpClient: Client[F]

  private val asyncF: Async[F] = implicitly[Async[F]]

  val logger: Logger[F] = Slf4jLogger.getLogger[F]

  def findTitle(url: String): F[Result[Title]] =
    for {
      html  <- httpClient.expect[String](url)
      title <- extractTitle(html, url).pure[F]
      _     <- logger.debug(s"[[${Thread.currentThread().getName}]] found title $title")
    } yield title.map(txt =>
      Title(
        id = UUID.randomUUID(),
        url = url,
        value = txt,
        createdAt = Instant.now(),
        taskId = None
      )
    )

  private def extractTitle(html: String, url: String): Result[String] =
    Try(html.substring(html.indexOf("<title>") + 7, html.indexOf("</title>")))
      .fold(err => Left(SiteParserError(url, None, err.getMessage)), Right(_))

}
