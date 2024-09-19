package ru.makaresh.siteparser.title.service

import cats.effect.Async
import cats.effect.implicits.*
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
      taskOpt     <- taskService.findById(taskId)
      nonEmptyTask = getNonEmptyTask(taskOpt)(TaskNotFoundError(s"Task with id: $taskId not found"))
      titles      <- nonEmptyTask.fold(
                       err => Left(err).pure[F],
                       t => titleRepository.findByTaskId(t.id, limit, offset).map(Right(_))
                     )
    } yield {
      for {
        task   <- nonEmptyTask
        result <- titles.map(t => GetByTaskIdResponse(task.id, task.status.toString, makeSuccessResponse(t)))
      } yield result
    }

  def getSiteTitles(
    urls: List[String]
  ): F[ApiResult[GetSiteNamePageSyncResponse]] =
    processSiteTitles(urls)
      .map((success, error) =>
        GetSiteNamePageSyncResponse(makeSuccessResponse(success), makeErrorResponse(error)).asRight[ApiError]
      )

  def getSiteTitlesAsync(urls: List[String]): F[ApiResult[GetSiteNamePageAsyncResponse]] =
    for {
      task    <- taskService.createTask(urls)
      nonEmpty = getNonEmptyTask(task)(TaskCreationError("Error creation task"))
      _       <- processSiteTitlesAsync(urls, nonEmpty.map(_.id)).start
    } yield nonEmpty.map(t => GetSiteNamePageAsyncResponse(t.id))

  private def makeSuccessResponse(titles: List[Title]): List[SiteNameSuccessResponse] =
    titles
      .map(title =>
        SiteNameSuccessResponse(
          title.url,
          title.value
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
