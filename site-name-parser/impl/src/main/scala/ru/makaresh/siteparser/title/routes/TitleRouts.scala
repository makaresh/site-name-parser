package ru.makaresh.siteparser.title.routes

import cats.effect.kernel.{Async, Concurrent}
import cats.implicits.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.io.*
import ru.makaresh.siteparser.api.title.request.*
import ru.makaresh.siteparser.api.title.response.*
import ru.makaresh.siteparser.common.ApiResult
import ru.makaresh.siteparser.title.model.Title
import ru.makaresh.siteparser.title.service.TitleService

import java.util.UUID
import scala.util.Try

/**
 * @author Bannikov Makar
 */
class TitleRouts[F[_]: Async](service: TitleService[F]) extends Http4sDsl[F] with TitleEntityFormat[F] {

  private val getByTaskId: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "title" :? TaskId(taskId) +& Limit(limit) +& Offset(offset) =>
      for {
        result   <- service.findByTaskId(taskId, limit, offset)
        response <- makeResponse(result)
      } yield response
  }

  private val getSiteTitles: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "title" / "GetSiteName" =>
      for {
        request  <- req.as[GetSiteName]
        result   <- service.getSiteTitles(request.urls)
        response <- makeResponse(result)
      } yield response
  }

  private val getSiteTitlesAsync: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "title" / "GetSiteNameAsync" =>
      for {
        request  <- req.as[GetSiteNameAsync]
        result   <- service.getSiteTitlesAsync(request.urls)
        response <- makeResponse(result)
      } yield response
  }

  private def makeResponse[R <: ApiOutput](
    result: ApiResult[R]
  )(using encoder: EntityEncoder[F, R]): F[Response[F]] = {

    result match {
      case Right(value)                     => Ok(value)
      case Left(TaskCreationError(message)) => InternalServerError(message)
      case Left(TaskNotFoundError(message)) => NotFound(message)
      case Left(TaskNotReadyError(message)) => BadRequest(message)
      case Left(TaskFailedError(message))   => InternalServerError(message)
      case _                                => InternalServerError("Internal Server Error")
    }
  }

  def allRoutes: HttpRoutes[F] =
    getByTaskId <+> getSiteTitles <+> getSiteTitlesAsync
}

trait TitleEntityFormat[F[_]: Concurrent] {

  given QueryParamDecoder[UUID] =
    QueryParamDecoder[String].emap { str =>
      Try(UUID.fromString(str)).toEither.leftMap { tr =>
        ParseFailure(tr.getMessage, tr.getMessage)
      }
    }

  object Id     extends QueryParamDecoderMatcher[UUID]("id")
  object TaskId extends QueryParamDecoderMatcher[UUID]("taskId")
  object Limit  extends QueryParamDecoderMatcher[Int]("limit")
  object Offset extends QueryParamDecoderMatcher[Int]("offset")

  given EntityDecoder[F, Title] = jsonOf[F, Title]
  given EntityEncoder[F, Title] = jsonEncoderOf[F, Title]

  given EntityDecoder[F, GetSiteName] = jsonOf[F, GetSiteName]
  given EntityEncoder[F, GetSiteName] = jsonEncoderOf[F, GetSiteName]

  given EntityDecoder[F, GetSiteNameAsync] = jsonOf[F, GetSiteNameAsync]
  given EntityEncoder[F, GetSiteNameAsync] = jsonEncoderOf[F, GetSiteNameAsync]

  given EntityDecoder[F, SiteNameErrorResponse] = jsonOf[F, SiteNameErrorResponse]
  given EntityEncoder[F, SiteNameErrorResponse] = jsonEncoderOf[F, SiteNameErrorResponse]

  given EntityDecoder[F, SiteNameSuccessResponse] = jsonOf[F, SiteNameSuccessResponse]
  given EntityEncoder[F, SiteNameSuccessResponse] = jsonEncoderOf[F, SiteNameSuccessResponse]

  given EntityDecoder[F, GetSiteNamePageSyncResponse] = jsonOf[F, GetSiteNamePageSyncResponse]
  given EntityEncoder[F, GetSiteNamePageSyncResponse] = jsonEncoderOf[F, GetSiteNamePageSyncResponse]

  given EntityDecoder[F, GetSiteNamePageAsyncResponse] = jsonOf[F, GetSiteNamePageAsyncResponse]
  given EntityEncoder[F, GetSiteNamePageAsyncResponse] = jsonEncoderOf[F, GetSiteNamePageAsyncResponse]

  given EntityDecoder[F, GetByTaskIdResponse] = jsonOf[F, GetByTaskIdResponse]
  given EntityEncoder[F, GetByTaskIdResponse] = jsonEncoderOf[F, GetByTaskIdResponse]
}
