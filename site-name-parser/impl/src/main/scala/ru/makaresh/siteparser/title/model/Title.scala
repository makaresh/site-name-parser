package ru.makaresh.siteparser.title.model

import io.circe.*

import java.time.Instant
import java.util.UUID

case class Title(
  id: UUID,
  url: String,
  value: String,
  createdAt: Instant,
  taskId: Option[UUID]
) derives Codec:
  def withTaskId(taskId: Option[UUID]): Title = copy(taskId = taskId)
