package ru.makaresh.siteparser.common

import cats.effect.kernel.Async
import com.zaxxer.hikari.HikariDataSource
import doobie.hikari.HikariTransactor
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.ClassicConfiguration
import org.scalatest.*
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import pureconfig.ConfigSource
import ru.makaresh.siteparser.config.AppConfig

import scala.concurrent.ExecutionContext

/**
 * @author Bannikov Makar
 */
trait BaseSpec[F[_]: Async]
  extends AnyFlatSpecLike
    with Matchers
    with BeforeAndAfter
    with BeforeAndAfterAll
    with GivenWhenThen {

  val config = ConfigSource.default.loadOrThrow[AppConfig]

  val dataSource = new HikariDataSource()
  dataSource.setDriverClassName(config.db.driver)
  dataSource.setJdbcUrl(config.db.url)
  dataSource.setPassword(config.db.password)
  dataSource.setUsername(config.db.user)

  val configuration = new ClassicConfiguration()
  configuration.setLocationsAsStrings("classpath:db/migration-test")
  configuration.setDataSource(dataSource)

  val migration  = Flyway(configuration).migrate()
  val transactor = HikariTransactor[F](dataSource, ExecutionContext.global)

  override protected def afterAll(): Unit = {
    super.afterAll()
    dataSource.close()
  }
}
