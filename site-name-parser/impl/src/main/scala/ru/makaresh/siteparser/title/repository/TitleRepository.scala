package ru.makaresh.siteparser.title.repository

import cats.effect.kernel.Async
import cats.implicits.*
import doobie.implicits.{toConnectionIOOps, toSqlInterpolator}
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.util.update.Update
import doobie.{ConnectionIO, Transactor}
import ru.makaresh.siteparser.title.model.Title

import java.util.UUID

class TitleRepository[F[_]: Async](transactor: Transactor[F]) {

  import TitleRepository.*

  def findByTaskId(taskId: UUID, limit: Int, offset: Int): F[List[Title]] =
    selectByTaskId(taskId, limit, offset).transact(transactor)

  def save(title: Title): F[Option[Title]] =
    insert(title).transact(transactor)

  def save(titles: List[Title]): F[List[Title]] =
    batchInsert(titles).transact(transactor)
}

private object TitleRepository:

  private def selectByTaskId(taskId: UUID, limit: Int, offset: Int): ConnectionIO[List[Title]] =
    sql"select * from title where task_id = $taskId limit $limit offset $offset".query[Title].to[List]

  private def insert(title: Title): ConnectionIO[Option[Title]] =
    sql"""
         insert into title (id, url, value, created_at, task_id)
         values (${title.id}, ${title.url}, ${title.value}, ${title.createdAt}, ${title.taskId})
         """.update
      .withGeneratedKeys[Title]("id", "url", "value", "created_at", "task_id")
      .compile
      .toList
      .map(_.headOption)

  private def batchInsert(titles: List[Title]): ConnectionIO[List[Title]] = {
    val query =
      """
         insert into title (id, url, value, created_at, task_id)
         values (?, ?, ?, ?, ?)
         """
    Update[Title](query)
      .updateManyWithGeneratedKeys[Title]("id", "url", "value", "created_at", "task_id")
      .apply(titles)
      .compile
      .toList
  }
