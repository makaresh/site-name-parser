package ru.makaresh.siteparser.task.service

import cats.effect.kernel.Async
import cats.implicits.*
import io.circe.syntax.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import ru.makaresh.siteparser.api.title.Result
import ru.makaresh.siteparser.task.model.Task
import ru.makaresh.siteparser.task.model.TaskStatus.New
import ru.makaresh.siteparser.task.repository.TaskRepository

import java.time.Instant
import java.util.UUID

class TaskService[F[_]: Async](repository: TaskRepository[F]) {

  val logger: Logger[F] = Slf4jLogger.getLogger[F]

  def findById(id: UUID): F[Result[Option[Task]]] =
    repository.findById(id).map(Right(_))

  def createTask(data: List[String]): F[Result[Option[Task]]] =
    for {
      task  <- makeTask(data)
      saved <- repository.save(task)
      _     <- logger.info(s"Created task: $saved")
    } yield Right(saved)

  def startTask(id: UUID): F[Result[Option[Task]]] =
    repository.startTask(id).map(Right(_))

  def finishTask(id: UUID, isSuccess: Boolean): F[Result[Option[Task]]] =
    repository.finishTask(id, Instant.now(), isSuccess).map(Right(_))

  private def makeTask(data: List[String]): F[Task] =
    Task(
      id = UUID.randomUUID(),
      status = New,
      createdAt = Instant.now(),
      taskData = data.asJson.noSpaces,
      finishedAt = None
    ).pure[F]
}
