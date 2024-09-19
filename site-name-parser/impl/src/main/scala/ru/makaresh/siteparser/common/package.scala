package ru.makaresh.siteparser

import ru.makaresh.siteparser.api.title.response.*

/**
 * Support types
 * 
 * @author Bannikov Makar
 */
package object common {

  type ApiResult[R <: ApiOutput] = Either[ApiError, R]
  type Result[R]                 = Either[SiteParserError, R]

}
