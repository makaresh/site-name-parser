package ru.makaresh.siteparser.task

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import doobie.implicits.*
import ru.makaresh.siteparser.common.BaseSpec
import ru.makaresh.siteparser.task.model.{Task, TaskStatus}
import ru.makaresh.siteparser.task.model.TaskStatus.{New, Processing, Success, Error}
import ru.makaresh.siteparser.task.module.TaskModule
import io.circe.syntax.*

import java.time.Instant
import java.util.UUID

/**
 * Task test
 *
 * @author Bannikov Makar
 */
class TaskSpec extends BaseSpec[IO] {

  given IORuntime = IORuntime.global

  val module = new TaskModule[IO](transactor)

  after {
    sql"delete from task".update.run
      .transact(transactor)
      .unsafeRunSync()
  }

  behavior of "Task Repository"

  it should "save and read new task" in {
    Given("Task entity")
    val taskId       = UUID.randomUUID()
    val creationTime = Instant.now()
    val task         = Task(taskId, New, Instant.now(), "some data", None)

    Then("Save task")
    val saved = module.repository.save(task).unsafeRunSync()
    Then("result should be equals expected data")
    saved shouldBe Some(task)

    Then("Find saved task")
    val found = module.repository.findById(taskId).unsafeRunSync()
    Then("result should be equals saved data")
    found shouldBe saved
  }

  it should "move saved task to processing" in {
    Given("Task entity")
    val taskId       = UUID.randomUUID()
    val creationTime = Instant.now()
    val task         = Task(taskId, New, Instant.now(), "some data", None)

    Then("Save task")
    val saved = module.repository.save(task).unsafeRunSync()
    Then("result should be equals expected data")
    saved shouldBe Some(task)

    Then("Move task to processing")
    val processing = module.repository.startTask(taskId).unsafeRunSync()
    processing shouldBe saved.map(_.copy(status = Processing))

    Then("Find updated task")
    val found = module.repository.findById(taskId).unsafeRunSync()
    Then("result should be equals updated data")
    found shouldBe processing
  }

  it should "move saved task to success" in {
    Given("Task entity")
    val taskId       = UUID.randomUUID()
    val creationTime = Instant.now()
    val task         = Task(taskId, Processing, Instant.now(), "some data", None)

    Then("Save task")
    val saved = module.repository.save(task).unsafeRunSync()
    Then("result should be equals expected data")
    saved shouldBe Some(task)

    Then("Move task to success")
    val finished = Instant.now()
    val success  = module.repository.finishTask(taskId, finished, true).unsafeRunSync()
    success shouldBe saved.map(_.copy(status = Success, finishedAt = Some(finished)))

    Then("Find updated task")
    val found = module.repository.findById(taskId).unsafeRunSync()
    Then("result should be equals updated data")
    found shouldBe success
  }

  it should "move saved task to error" in {
    Given("Task entity")
    val taskId       = UUID.randomUUID()
    val creationTime = Instant.now()
    val task         = Task(taskId, Processing, Instant.now(), "some data", None)

    Then("Save task")
    val saved = module.repository.save(task).unsafeRunSync()
    Then("result should be equals expected data")
    saved shouldBe Some(task)

    Then("Move task to error")
    val finished = Instant.now()
    val error    = module.repository.finishTask(taskId, finished, false).unsafeRunSync()
    error shouldBe saved.map(_.copy(status = Error, finishedAt = Some(finished)))

    Then("Find updated task")
    val found = module.repository.findById(taskId).unsafeRunSync()
    Then("result should be equals updated data")
    found shouldBe error
  }

  behavior of "Task service"

  it should "create task" in {
    val saved     = module.service.createTask(List("some data", "another data")).unsafeRunSync()
    Then("result should be equals expected data")
    saved.isRight shouldBe true
    val savedData = saved.toOption.get
    savedData.status shouldBe New
    savedData.taskData shouldBe List("some data", "another data").asJson.noSpaces

    Then("Find created task")
    val found     = module.service.findById(savedData.id).unsafeRunSync()
    Then("result should be equals updated data")
    found.isRight shouldBe true
    val foundData = found.toOption.get
    foundData shouldBe savedData
  }

  it should "finish task" in {
    Given("Processing task")
    val taskId       = UUID.randomUUID()
    val creationTime = Instant.now()
    val task         = Task(taskId, Processing, Instant.now(), "some data", None)
    module.repository.save(task).unsafeRunSync()

    Then("Move task to success")
    val success     = module.service.finishTask(taskId, true).unsafeRunSync()
    success.isRight shouldBe true
    val successData = success.toOption.get
    successData.status shouldBe Success
    successData.finishedAt.nonEmpty shouldBe true

    Then("Find updated task")
    val found = module.repository.findById(taskId).unsafeRunSync()
    Then("result should be equals updated data")
    found shouldBe Some(successData)
  }
}
