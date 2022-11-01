package posting.logic

import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import io.chrisdavenport.epimetheus.Summary.Quantile.quantile
import io.chrisdavenport.epimetheus._
import shapeless.Sized

import scala.concurrent.duration._

private[posting] trait PostProcessingMetrics[F[_]] {
  def measureProcessingDuration(duration: FiniteDuration, postType: String): F[Unit]
  def countCallsForProcessing(postsNumber: Int): F[Unit]
  def countProcessedCalls(): F[Unit]
}

private[posting] object PostProcessingMetrics {
  def of[F[_]: Sync](registry: CollectorRegistry[F]): F[PostProcessingMetrics[F]] =
    for {
      summary     <- processingTimeSummary(registry)
      callCounter <- callsCounter(registry)
      postCounter <- allProcessedPostCounter(registry)
    } yield new PostProcessingMetrics[F] {
      override def measureProcessingDuration(duration: FiniteDuration, postType: String): F[Unit] =
        summary.label(postType).observe(duration.toMillis.toDouble)

      override def countCallsForProcessing(postNumber: Int): F[Unit] = postCounter.incBy(postNumber.toDouble)

      override def countProcessedCalls(): F[Unit] = callCounter.inc
    }

  private def callsCounter[F[_]: Sync](registry: CollectorRegistry[F]): F[Counter[F]] =
    Counter.noLabels(
      cr = registry,
      name = Name("json_posts_processing_calls_sum"),
      help = "Number of calls for processing",
    )

  private def allProcessedPostCounter[F[_]: Sync](registry: CollectorRegistry[F]): F[Counter[F]] =
    Counter.noLabels(
      cr = registry,
      name = Name("json_posts_all_posts_sum"),
      help = "Number of all processed posts",
    )

  private def processingTimeSummary[F[_]: Sync](
    registry: CollectorRegistry[F],
  ) = Summary.labelled(
    cr = registry,
    name = Name("json_posts_processing_duration"),
    help = "Summary of the processing duration in milliseconds distinguishing if posts are with comments or not",
    labels = Sized(Label("post_type")),
    (label: String) => Sized(label),
    quantiles = quantile(0.5, 0.1),
    quantile(0.9, 0.1),
    quantile(0.99, 0.1),
  )
}
