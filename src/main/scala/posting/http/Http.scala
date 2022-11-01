package posting.http

import cats.effect.{Async, Resource}
import cats.syntax.semigroupk._
import org.http4s.HttpApp
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.client.Client
import posting.http.HttpConfig.{HttpClientConfig, HttpServerConfig}

import scala.concurrent.ExecutionContext

private[posting] object Http {
  def server[F[_]: Async](
    config: HttpServerConfig,
    blockingContext: ExecutionContext,
    healthAndMetricsRoutes: FundamentalRoutes[F],
    processingRoutes: ProcessingRoutes[F],
  ) = new Server[F](config, blockingContext, healthAndMetricsRoutes, processingRoutes)

  def client[F[_]: Async](config: HttpClientConfig, blockingPool: ExecutionContext): Resource[F, Client[F]] =
    BlazeClientBuilder[F]
      .withExecutionContext(blockingPool)
      .withConnectTimeout(config.connectionTimeout)
      .withRequestTimeout(config.requestTimeout)
      .withIdleTimeout(config.idleTimeout)
      .resource

  class Server[F[_]: Async](
    config: HttpServerConfig,
    blockingContext: ExecutionContext,
    healthAndMetricsRoutes: FundamentalRoutes[F],
    processingRoutes: ProcessingRoutes[F],
  ) {
    def start(): F[Unit] =
      BlazeServerBuilder[F]
        .withExecutionContext(blockingContext)
        .bindHttp(config.port, config.host)
        .withIdleTimeout(config.idleTimeout)
        .withResponseHeaderTimeout(config.responseHeaderTimeout)
        .withHttpApp(app)
        .serve
        .compile
        .drain

    private val app: HttpApp[F] =
      (healthAndMetricsRoutes.routes <+> processingRoutes.routes).orNotFound
  }
}
