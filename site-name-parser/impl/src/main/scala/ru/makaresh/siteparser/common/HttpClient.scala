package ru.makaresh.siteparser.common

import cats.effect.Async
import cats.implicits.*
import org.http4s.Status.Successful
import org.http4s.client.Client
import org.http4s.{EntityDecoder, Response}

/**
 * Wrapper for Client
 *
 * @author Bannikov Makar
 */
class HttpClient[F[_]: Async](client: Client[F]) {

  def runGetRequest[E, R](url: String)(onError: Response[F] => E)(using
    EntityDecoder[F, R]
  ): F[Either[E, R]] =
    client.get[Either[E, R]](url) {
      case Successful(response) =>
        response.as[R].map(Right(_))
      case failedResponse       =>
        failedResponse.as[R].map(_ => Left(onError(failedResponse)))
    }
}
