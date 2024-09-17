package ru.makaresh.siteparser.task.repository

import cats.effect.kernel.Async
import doobie.*
import doobie.implicits.legacy.instant.*
import doobie.implicits.*
import doobie.postgres.implicits.*
import ru.makaresh.siteparser.task.model.{Task, TaskStatus}

import java.time.Instant
import java.util.UUID

class TaskRepository[F[_]: Async](transactor: Transactor[F]) {

  import TaskRepository.*

  def findById(id: UUID): F[Option[Task]] =
    selectById(id).transact(transactor)

  def save(task: Task): F[Option[Task]] =
    insert(task).transact(transactor)

  def startTask(id: UUID): F[Option[Task]] =
    updateStart(id).transact(transactor)

  def finishTask(id: UUID, finishedAt: Instant, isSuccess: Boolean): F[Option[Task]] =
    updateFinish(id, finishedAt, isSuccess).transact(transactor)
}

private object TaskRepository:

  private def selectById(id: UUID): ConnectionIO[Option[Task]] =
    sql"""select * from task where id = $id""".query[Task].option

  private def insert(task: Task): ConnectionIO[Option[Task]] =
    sql"""
           insert into task (id, status, created_at, task_data, finished_at)
           values (${task.id}, ${task.status}, ${task.createdAt}, ${task.taskData}, ${task.finishedAt})
           """.update
      .withGeneratedKeys[Task]("id", "status", "created_at", "task_data", "finished_at")
      .compile
      .toList
      .map(_.headOption)

  private def updateStart(id: UUID): ConnectionIO[Option[Task]] =
    sql"""
          update task
          set status = ${TaskStatus.Processing}
          where id = $id
         """.update
      .withGeneratedKeys[Task]("id", "status", "created_at", "task_data", "finished_at")
      .compile
      .toList
      .map(_.headOption)

  private def updateFinish(id: UUID, finishedAt: Instant, isSuccess: Boolean): ConnectionIO[Option[Task]] =
    val status = if (isSuccess) TaskStatus.Success else TaskStatus.Error
    sql"""
            update task
            set status = $status, finished_at = $finishedAt
            where id = $id
           """.update
      .withGeneratedKeys[Task]("id", "status", "created_at", "task_data", "finished_at")
      .compile
      .toList
      .map(_.headOption)
