package ru.makaresh.siteparser.task.module

import cats.effect.kernel.Async
import doobie.Transactor
import ru.makaresh.siteparser.task.repository.TaskRepository
import ru.makaresh.siteparser.task.service.TaskService

/**
 * @author Bannikov Makar
 */
class TaskModule[F[_]: Async](transactor: Transactor[F]) {

  val repository = new TaskRepository[F](transactor)
  val service    = new TaskService[F](repository)

}
