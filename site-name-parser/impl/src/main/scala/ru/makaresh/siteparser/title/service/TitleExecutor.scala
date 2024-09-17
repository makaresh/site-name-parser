package ru.makaresh.siteparser.title.service

import cats.effect.implicits.*
import cats.effect.kernel.Async
import cats.implicits.*
import ru.makaresh.siteparser.api.title.Result
import ru.makaresh.siteparser.api.title.response.{ApiError, SiteParserError}
import ru.makaresh.siteparser.task.service.TaskService
import ru.makaresh.siteparser.title.model.Title
import ru.makaresh.siteparser.title.repository.TitleRepository

import java.util.UUID

trait TitleExecutor[F[_]: Async] extends TitleParser[F] {

  def titleRepository: TitleRepository[F]
  def taskService: TaskService[F]
  def titleBatch: Int

  def processSiteTitles(
    urls: List[String],
    taskId: Option[UUID] = None
  ): F[(List[Title], List[SiteParserError])] =
    for {
      results: List[Result[Title]] <- urls.parTraverse(findTitle)
      error: List[SiteParserError]  = results.collect { case Left(value) => value }
      success: List[Title]          = results.collect { case Right(value) => value }
      saved                        <- titleRepository.save(success.map(_.withTaskId(taskId)))
    } yield (saved, error)

  def processSiteTitlesAsync(urls: List[String], taskId: Either[ApiError, UUID]): F[Unit] =
    taskId match {
      case Right(id) =>
        for {
          task  <- taskService.startTask(id)
          _     <- logger.info(s"Started task: $task")
          parted = urls.grouped(titleBatch).toList
          a     <- parted.traverse(processSiteTitles(_, Some(id)))
          task  <- taskService.finishTask(id, true)
          _     <- logger.info(s"Task finished: $task")
        } yield ()
      case _         => ().pure[F]
    }

}
