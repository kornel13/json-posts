package posting

import cats.NonEmptyParallel
import cats.effect._
import cats.syntax.flatMap._
import cats.syntax.functor._
import ch.qos.logback.classic.LoggerContext
import io.chrisdavenport.epimetheus.{Collector, CollectorRegistry}
import org.http4s.client.{Client => HttpClient}
import org.slf4j.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jLogger
import posting.http.{FundamentalRoutes, Http, ProcessingRoutes}
import posting.logic.PostProcessor

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}

object Main extends IOApp {
  final case class Resources[F[_]](serverBlockingContext: ExecutionContext, httpClient: HttpClient[F])

  override def run(args: List[String]): IO[ExitCode] = {
    val app = for {
      config       <- AppConfig.load[IO]
      registry     = CollectorRegistry.defaultRegistry[IO]
      _            <- Collector.Defaults.registerDefaults(registry)
      appResources = resources[IO](config)
      app          <- appResources.use(startApp[IO](config, registry, _).as(ExitCode.Success))
    } yield app

    app
      .onError(error => Slf4jLogger.create[IO].flatMap(_.error(error)("Failed to start")))
      .guarantee(flushLogs[IO])
  }

  private def startApp[F[_]: Async: NonEmptyParallel](
    config: AppConfig,
    registry: CollectorRegistry[F],
    appResources: Resources[F],
  ): F[Unit] = {
    for {
      postProcessor          <- PostProcessor.of(config.post, appResources.httpClient)
      healthAndMetricsRoutes = new FundamentalRoutes(registry)
      processingRoutes       = new ProcessingRoutes(postProcessor)
      httpServer = Http.server(
        config.http.server,
        appResources.serverBlockingContext,
        healthAndMetricsRoutes,
        processingRoutes,
      )
      app <- httpServer.start()
    } yield app
  }

  private def flushLogs[F[_]: Sync] = Sync[F].delay(LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext].stop())

  private def resources[F[_]: Async](config: AppConfig): Resource[F, Resources[F]] =
    for {
      serverPool     <- blockingContext(config.http.server.blockingPoolSize)
      httpClientPool <- blockingContext(config.http.client.blockingPoolSize)
      httpClient     <- Http.client(config.http.client, httpClientPool)
    } yield Resources(serverPool, httpClient)

  private def blockingContext[F[_]: Sync](poolSize: Int): Resource[F, ExecutionContextExecutorService] =
    Resource.make(
      Sync[F].delay(
        ExecutionContext
          .fromExecutorService(Executors.newFixedThreadPool(poolSize)),
      ),
    )(ec => Sync[F].delay(ec.shutdown()))
}
