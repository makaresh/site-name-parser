package ru.makaresh.siteparser.config

import pureconfig.ConfigReader
import pureconfig.generic.derivation.default.*

/**
 * @author Bannikov Makar
 */
case class AppConfig(api: ApiConfig, db: DbConfig, titleBatch: Int) derives ConfigReader

case class ApiConfig(
  host: String,
  port: Int
) derives ConfigReader

case class DbConfig(
  host: String,
  port: Int,
  user: String,
  password: String,
  databaseName: String,
  url: String,
  driver: String,
  pool: Int
) derives ConfigReader

