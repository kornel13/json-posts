package posting.logic

import cats.effect.kernel.Async
import cats.syntax.either._
import cats.syntax.flatMap._
import cats.syntax.functor._
import fs2.Stream
import org.http4s.Method.GET
import org.http4s.Uri.Path
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.client.Client
import org.http4s.headers.Accept
import org.http4s.{EntityDecoder, Headers, MediaType, Request, Response}
import org.typelevel.log4cats.slf4j.Slf4jLogger
import posting.domain.Domain.{Comment, Post}
import posting.domain.DomainJson._
import posting.domain.ProcessingError
import posting.domain.ProcessingError.ErrorOr
import posting.domain.ProcessingError.ProcessingErrorType.{DecodingError, ServerConnectionError}

private[posting] trait PostReader[F[_]] {
  def getPosts: F[ErrorOr[List[Post]]]
  def getComments: F[ErrorOr[List[Comment]]]
}

private[posting] object PostReader {
  def of[F[_]: Async](config: PostConfig, httpClient: Client[F]): F[PostReader[F]] =
    Slf4jLogger
      .create[F]
      .map(logger =>
        new PostReader[F] {
          override def getPosts: F[ErrorOr[List[Post]]] =
            httpClient.run(request(config.reader.postPath)).use(handleResponse[List[Post]])

          override def getComments: F[ErrorOr[List[Comment]]] =
            httpClient.run(request(config.reader.commentPath)).use(handleResponse[List[Comment]])

          private def handleResponse[A](response: Response[F])(implicit entityDecoder: EntityDecoder[F, A]) =
            if (response.status.isSuccess) {
              Stream
                .eval(entityDecoder.decode(response, strict = false).value)
                .evalTap {
                  case Left(error) => logger.error(error)("Cannot decode the response")
                  case Right(_)    => Async[F].unit
                }
                .map(_.leftMap(error => ProcessingError(DecodingError, error.getMessage)))
                .compile
                .lastOrError
            } else
              for {
                error <- errorResponse(response)
                _     <- logger.error(error)
              } yield ProcessingError(ServerConnectionError, error).asLeft[A]

          private def errorResponse(response: Response[F]): F[String] =
            response.bodyText.compile.string.map(body => s"Http code: ${response.status.code} Error: $body")

          private def request(path: Path) = Request[F](
            method = GET,
            uri = config.reader.uri.withPath(path),
            headers = Headers(
              Accept(MediaType.application.json),
            ),
          )
        },
      )
}
