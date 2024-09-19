package ru.makaresh.siteparser.title.service

import cats.effect.Async
import cats.effect.implicits.*
import cats.implicits.*
import ru.makaresh.siteparser.api.title.response.*
import ru.makaresh.siteparser.task.service.TaskService
import ru.makaresh.siteparser.title.model.Title
import ru.makaresh.siteparser.title.repository.TitleRepository

import java.util.UUID

/**
 * @author Bannikov Makar
 */
trait TitleExecutor[F[_]: Async] extends TitleParser[F] {

  def titleRepository: TitleRepository[F]
  def taskService: TaskService[F]
  def titleBatch: Int

  def processSiteTitles(
    urls: List[String],
    taskId: Option[UUID] = None
  ): F[(List[Title], List[SiteParserError])] =
    for {
      results <- urls.parTraverse(findTitle)
      error    = results.collect { case Left(value) => value }
      success  = results.collect { case Right(value) => value }
      saved   <- titleRepository.save(success.map(_.withTaskId(taskId)))
    } yield (saved, error)

  def processSiteTitlesAsync(urls: List[String], taskId: Either[ApiError, UUID]): F[Unit] = {
    taskId match {
      case Right(id) =>
        val result = for {
          task  <- taskService.startTask(id)
          _     <- logger.info(s"Started task: $task")
          parted = urls.grouped(titleBatch).toList
          _     <- parted.traverse_(processSiteTitles(_, Some(id)))
          task  <- taskService.finishTask(id, true)
          _     <- logger.info(s"Task finished: $task")
        } yield ()
        result.recoverWith { case err =>
          for {
            _ <- logger.info(s"Task with id: $id failed ")
            _ <- logger.debug(err.getMessage)
            _ <- taskService.finishTask(id, false)
          } yield ()
        }
      case _         => ().pure[F]
    }
  }
}
