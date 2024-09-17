package ru.makaresh.siteparser.title.module

import cats.effect.kernel.Async
import doobie.Transactor
import org.http4s.HttpRoutes
import org.http4s.client.Client
import ru.makaresh.siteparser.task.service.TaskService
import ru.makaresh.siteparser.title.repository.TitleRepository
import ru.makaresh.siteparser.title.routes.TitleRouts
import ru.makaresh.siteparser.title.service.TitleService

class TitleModule[F[_]: Async](
  transactor: Transactor[F],
  taskService: TaskService[F],
  httpClient: Client[F],
  titleBatch: Int
) {

  val repository     = new TitleRepository[F](transactor)
  val service        = new TitleService[F](repository, taskService, httpClient, titleBatch)
  private val routes = new TitleRouts[F](service)

  def titleRoutes: HttpRoutes[F] = routes.allRoutes

}
