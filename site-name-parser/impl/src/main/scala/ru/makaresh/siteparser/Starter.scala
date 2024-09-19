package ru.makaresh.siteparser

import cats.effect.*

object Starter extends IOApp {

  /**
   * @author Bannikov Makar
   */
  override def run(args: List[String]): IO[ExitCode] =
    Server[IO]
      .use(_ => IO.never)
      .as(ExitCode.Success)
}
