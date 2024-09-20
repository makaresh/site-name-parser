package ru.makaresh.siteparser.db

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult

import javax.sql.DataSource

/**
 * @author Bannikov Makar
 */
object DbMigrator:

  def apply(dataSource: DataSource): MigrateResult =
    Flyway
      .configure()
      .dataSource(dataSource)
      .load()
      .migrate()
