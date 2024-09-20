package ru.makaresh.siteparser.title

import cats.effect.IO
import cats.effect.kernel.Resource
import cats.effect.unsafe.IORuntime
import doobie.implicits.*
import fs2.Stream
import org.http4s.client.Client
import org.http4s.{Headers, Response, Status}
import ru.makaresh.siteparser.api.title.response.{GetSiteNamePageSyncResponse, SiteNameSuccessResponse}
import ru.makaresh.siteparser.common.BaseSpec
import ru.makaresh.siteparser.task.model.Task
import ru.makaresh.siteparser.task.model.TaskStatus.Success
import ru.makaresh.siteparser.task.module.TaskModule
import ru.makaresh.siteparser.title.model.Title
import ru.makaresh.siteparser.title.module.TitleModule

import java.time.Instant
import java.util.UUID

/**
 * Title test
 *
 * @author Bannikov Makar
 */
class TitleSpec extends BaseSpec[IO] {

  given IORuntime = IORuntime.global

  def httpClient: Client[IO] = Client.apply[IO] { _ =>
    Resource.eval(IO(Response[IO](body = Stream.emits("<title>Super Name</title>".getBytes("UTF-8")))))
  }

  val taskModule = new TaskModule[IO](transactor)
  val module     = new TitleModule[IO](transactor, taskModule.service, httpClient, config.titleBatch)

  after {
    (for {
      _ <- sql"delete from title".update.run
             .transact(transactor)
      _ <- sql"delete from task".update.run
             .transact(transactor)
    } yield ()).unsafeRunSync()
  }

  behavior of "Task Repository"

  it should "save new title" in {
    Given("Title entity")
    val titleId      = UUID.randomUUID()
    val creationTime = Instant.now()
    val title        = Title(titleId, "http://somesitename.com", "Super Name", creationTime, None)

    Then("Save title")
    val saved = module.repository.save(title).unsafeRunSync()
    Then("result should be equals expected data")
    saved shouldBe Some(title)
  }

  it should "find saved title by success task id" in {
    Given("Title entity")
    val titleId      = UUID.randomUUID()
    val taskId       = UUID.randomUUID()
    val creationTime = Instant.now()
    val task         = Task(taskId, Success, creationTime, "some data", Some(Instant.now()))
    val title        = Title(titleId, "http://somesitename.com", "Super Name", creationTime, Some(taskId))

    Then("Save title")
    (for {
      _ <- taskModule.repository.save(task)
      _ <- module.repository.save(title)
    } yield ()).unsafeRunSync()

    val found = module.repository.findByTaskId(taskId, 5, 0).unsafeRunSync()
    found.nonEmpty shouldBe true
    found shouldBe List(title)
  }

  behavior of "Title Service"

  it should "get site titles" in {
    Given("list of urls")
    val urls = List("http://somesitename.com")

    Then("get titles of given list of urls")
    val result = module.service.getSiteTitles(urls).unsafeRunSync()
    result shouldBe Right(
      GetSiteNamePageSyncResponse(
        success = List(SiteNameSuccessResponse("http://somesitename.com", "Super Name"))
      )
    )
  }
  
}
