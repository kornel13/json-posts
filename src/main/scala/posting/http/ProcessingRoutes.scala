package posting.http

import cats.effect.Async
import cats.syntax.flatMap._
import io.circe.generic.semiauto.deriveDecoder
import io.circe.{Decoder, Encoder}
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDsl
import posting.domain.ProcessingError.ErrorOr
import posting.http.ProcessingRoutes.PostRequest
import posting.logic.PostProcessor

private[posting] class ProcessingRoutes[F[_]: Async](postProcessor: PostProcessor[F]) extends Http4sDsl[F] {

  def routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case request @ POST -> Root / "post" =>
      request.decode[PostRequest](postRequest =>
        if (postRequest.withComments)
          postProcessor.savePostsWithComments().flatMap(handleResponse[String])
        else
          postProcessor.savePosts().flatMap(handleResponse[String]),
      )
  }

  private def handleResponse[A: Encoder](response: ErrorOr[A]) =
    response match {
      case Left(error) => InternalServerError(error.httpBody)
      case Right(data) => Ok(data)
    }
}

private[posting] object ProcessingRoutes {
  final case class PostRequest(withComments: Boolean)
  object PostRequest {
    implicit val decoder: Decoder[PostRequest] = deriveDecoder
  }
}
