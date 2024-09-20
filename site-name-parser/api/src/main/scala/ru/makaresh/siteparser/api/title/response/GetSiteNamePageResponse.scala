package ru.makaresh.siteparser.api.title.response

import io.circe.Codec

import java.util.UUID

trait ApiOutput

/**
 * Responses dto
 *
 * @author Bannikov Makar
 */
case class GetSiteNamePageSyncResponse(
  success: List[SiteNameSuccessResponse],
  error: List[SiteNameErrorResponse] = List.empty
) extends ApiOutput
  derives Codec

case class SiteNameSuccessResponse(url: String, title: String) extends ApiOutput derives Codec

case class SiteNameErrorResponse(url: String, code: Option[Int], message: String) extends ApiOutput
  derives Codec

case class GetSiteNamePageAsyncResponse(taskId: UUID) extends ApiOutput derives Codec

case class GetByTaskIdResponse(taskId: UUID, taskStatus: String, data: List[SiteNameSuccessResponse])
  extends ApiOutput derives Codec

case class SiteParserError(url: String, code: Option[Int], message: String)

trait ApiError

case class TaskCreationError(message: String)                   extends ApiError
case class TaskNotFoundError(message: String)                   extends ApiError
case class TaskNotReadyError(message: String)                   extends ApiError
case class TaskFailedError(message: String)                     extends ApiError
case class TaskExecutionError(message: String, cause: ApiError) extends ApiError
case class TooManyUrlsError(message: String)                    extends ApiError
