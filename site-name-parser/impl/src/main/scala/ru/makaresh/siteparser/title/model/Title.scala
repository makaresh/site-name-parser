package ru.makaresh.siteparser.title.model

import io.circe.*

import java.time.Instant
import java.util.UUID

/**
 * Title entity
 *
 * @author Bannikov Makar
 */
case class Title(
  id: UUID,
  url: String,
  titleValue: String,
  createdAt: Instant,
  taskId: Option[UUID]
) derives Codec:
  def withTaskId(taskId: Option[UUID]): Title = copy(taskId = taskId)
