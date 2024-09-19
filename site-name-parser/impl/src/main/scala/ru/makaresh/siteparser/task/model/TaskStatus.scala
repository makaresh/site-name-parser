package ru.makaresh.siteparser.task.model

import doobie.util.{Get, Put}
import io.circe.Codec

/**
 * @author Bannikov Makar
 */
enum TaskStatus derives Codec:
  case New, Processing, Success, Error

object TaskStatus:
  given Get[TaskStatus] = Get[String].map(TaskStatus.valueOf)
  given Put[TaskStatus] = Put[String].contramap(_.toString)
