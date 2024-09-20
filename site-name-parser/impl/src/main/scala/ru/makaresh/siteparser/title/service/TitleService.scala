package ru.makaresh.siteparser.title.service

import cats.effect.Async
import cats.effect.implicits.*
import cats.effect.std.Supervisor
import cats.implicits.*
import ru.makaresh.siteparser.api.title.response.*
import ru.makaresh.siteparser.common.*
import ru.makaresh.siteparser.task.*
import ru.makaresh.siteparser.task.model.Task
import ru.makaresh.siteparser.task.service.TaskService
import ru.makaresh.siteparser.title.model.Title
import ru.makaresh.siteparser.title.repository.TitleRepository

import java.util.UUID

/**
 * @author Bannikov Makar
 */
class TitleService[F[_]: Async](
  val titleRepository: TitleRepository[F],
  val taskService: TaskService[F],
  val httpClient: HttpClient[F],
  val titleBatch: Int
) extends TitleExecutor[F] {

  def findByTaskId(taskId: UUID, limit: Int, offset: Int): F[ApiResult[GetByTaskIdResponse]] =
    for {
      foundTask <- taskService.findById(taskId)
      titles    <- foundTask.fold(
                     err => Left(err).pure[F],
                     t => titleRepository.findByTaskId(t.id, limit, offset).map(Right(_))
                   )
    } yield {
      for {
        task   <- foundTask
        result <- titles.map(t => GetByTaskIdResponse(task.id, task.status.toString, makeSuccessResponse(t)))
      } yield result
    }

  def getSiteTitles(
    urls: List[String]
  ): F[ApiResult[GetSiteNamePageSyncResponse]] =
    if (urls.length <= titleBatch)
      processSiteTitles(urls)
      .map((success, error) =>
        GetSiteNamePageSyncResponse(makeSuccessResponse(success), makeErrorResponse(error)).asRight[ApiError]
      )
    else
      Left(TooManyUrlsError(
        s"""
           |Input urls array too big, allowed array size is $titleBatch.
           |Try GetSiteNameAsync request for async title extraction execution.
           |""".stripMargin)).pure[F]

  def getSiteTitlesAsync(urls: List[String]): F[ApiResult[GetSiteNamePageAsyncResponse]] =
    for {
      task <- taskService.createTask(urls)
      _    <- processSiteTitlesAsync(urls, task.map(_.id)).start
    } yield task.map(t => GetSiteNamePageAsyncResponse(t.id))

  private def makeSuccessResponse(titles: List[Title]): List[SiteNameSuccessResponse] =
    titles
      .map(title =>
        SiteNameSuccessResponse(
          title.url,
          title.titleValue
        )
      )

  private def makeErrorResponse(errors: List[SiteParserError]): List[SiteNameErrorResponse] =
    errors
      .map(error =>
        SiteNameErrorResponse(
          error.url,
          error.code,
          error.message
        )
      )

  private def getNonEmptyTask(task: Result[Option[Task]])(ifEmpty: ApiError): Either[ApiError, Task] =
    task.fold(
      _ => Either.left[ApiError, Task](ifEmpty),
      _.fold(Either.left[ApiError, Task](ifEmpty))(Either.right[ApiError, Task](_))
    )

}
