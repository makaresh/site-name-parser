package ru.makaresh.siteparser.api.title.request

import io.circe.Codec

/**
 * Request bodies
 * 
 * @author Bannikov Makar
 */
case class GetSiteName(urls: List[String]) derives Codec

case class GetSiteNameAsync(urls: List[String]) derives Codec
