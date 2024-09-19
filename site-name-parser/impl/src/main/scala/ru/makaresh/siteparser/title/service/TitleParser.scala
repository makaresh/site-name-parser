package ru.makaresh.siteparser.title.service

import cats.effect.kernel.Async
import cats.implicits.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import ru.makaresh.siteparser.api.title.response.SiteParserError
import ru.makaresh.siteparser.common.Result
import ru.makaresh.siteparser.common.HttpClient
import ru.makaresh.siteparser.title.model.Title

import java.time.Instant
import java.util.UUID
import scala.util.Try

/**
 * @author Bannikov Makar
 */
trait TitleParser[F[_]: Async] {

  def httpClient: HttpClient[F]

  val logger: Logger[F] = Slf4jLogger.getLogger[F]

  def findTitle(url: String): F[Result[Title]] =
    for {
      html  <- runRequest(url)
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

  private def runRequest(url: String): F[Result[String]] =
    httpClient
      .runGetRequest[SiteParserError, String](url) { failedResponse =>
        val status = failedResponse.status.code
        SiteParserError(url, Some(status), s"Request failed with status $status")
      }
      .recoverWith { case err =>
        logger.debug(err.getMessage).map { _ =>
          Left(SiteParserError(url, None, err.getMessage))
        }
      }

  private def extractTitle(html: Result[String], url: String): Result[String] =
    html.flatMap(str =>
      Either
        .fromTry(Try(str.substring(str.indexOf("<title>") + 7, str.indexOf("</title>"))))
        .leftMap(err => SiteParserError(url, None, err.getMessage))
    )

}
