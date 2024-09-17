package ru.makaresh.siteparser.api

import ru.makaresh.siteparser.api.title.response.{ApiError, ApiOutput, SiteParserError}

package object title {

  type ApiResult[R <: ApiOutput] = Either[ApiError, R]
  type Result[R]                 = Either[SiteParserError, R]

}
