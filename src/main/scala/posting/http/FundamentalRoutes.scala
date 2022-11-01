package posting.http

import cats.effect.Sync
import cats.syntax.functor._
import cats.syntax.flatMap._
import io.chrisdavenport.epimetheus.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`

private[posting] class FundamentalRoutes[F[_]: Sync](
  registry: CollectorRegistry[F],
) extends Http4sDsl[F] {

  private val contentType = Sync[F].fromEither(`Content-Type`.parse(TextFormat.CONTENT_TYPE_004))

  def routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "health" / "ready" => Ok("READY")
    case GET -> Root / "health" / "live"  => Ok("OK")
    case GET -> Root / "metrics" =>
      for {
        contentType <- contentType
        response    <- Ok(registry.write004)
      } yield response.withContentType(contentType)
  }
}
