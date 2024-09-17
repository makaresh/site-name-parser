package ru.makaresh.siteparser.api.title.request

import io.circe.Codec

case class GetSiteName(urls: List[String]) derives Codec

case class GetSiteNameAsync(urls: List[String]) derives Codec
