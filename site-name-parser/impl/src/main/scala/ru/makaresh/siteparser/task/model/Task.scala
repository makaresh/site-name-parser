package ru.makaresh.siteparser.task.model

import io.circe.Codec

import java.time.Instant
import java.util.UUID

/**
 * Task entity
 * 
 * @author Bannikov Makar
 */
case class Task(
  id: UUID,
  status: TaskStatus,
  createdAt: Instant,
  taskData: String,
  finishedAt: Option[Instant]
) derives Codec:
  def process(): Task = copy(status = TaskStatus.Processing)
  def success(): Task = copy(status = TaskStatus.Success)
  def error(): Task   = copy(status = TaskStatus.Error)
