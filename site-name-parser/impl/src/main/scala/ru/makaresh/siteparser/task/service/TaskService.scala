package ru.makaresh.siteparser.task.service

import cats.effect.kernel.Async
import cats.implicits.*
import io.circe.syntax.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import ru.makaresh.siteparser.api.title.response.{TaskCreationError, TaskNotFoundError}
import ru.makaresh.siteparser.task.model.Task
import ru.makaresh.siteparser.task.model.TaskStatus.New
import ru.makaresh.siteparser.task.repository.TaskRepository

import java.time.Instant
import java.util.UUID

/**
 * @author Bannikov Makar
 */
class TaskService[F[_]: Async](repository: TaskRepository[F]) {

  val logger: Logger[F] = Slf4jLogger.getLogger[F]

  def findById(id: UUID): F[Either[TaskNotFoundError, Task]] =
    repository.findById(id).map(Either.fromOption(_, TaskNotFoundError(s"Task with id: $id not found")))

  def createTask(data: List[String]): F[Either[TaskCreationError, Task]] =
    for {
      task  <- makeTask(data)
      saved <- repository.save(task)
      _     <- logger.info(s"Created task: $saved")
    } yield Either.fromOption(saved, TaskCreationError("Error creating task"))

  def startTask(id: UUID): F[Either[TaskNotFoundError, Task]] =
    repository.startTask(id).map(Either.fromOption(_, TaskNotFoundError(s"Task with id: $id not found")))

  def finishTask(id: UUID, isSuccess: Boolean): F[Either[TaskNotFoundError, Task]] =
    repository
      .finishTask(id, Instant.now(), isSuccess)
      .map(Either.fromOption(_, TaskNotFoundError(s"Task with id: $id not found")))

  private def makeTask(data: List[String]): F[Task] =
    Task(
      id = UUID.randomUUID(),
      status = New,
      createdAt = Instant.now(),
      taskData = data.asJson.noSpaces,
      finishedAt = None
    ).pure[F]
}
