package posting.logic

import better.files._
import cats.effect.kernel.Async
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import cats.syntax.functor._
import fs2.Stream
import io.circe.syntax._
import io.circe.{Encoder, Printer}
import org.typelevel.log4cats.slf4j.Slf4jLogger
import posting.domain.Domain.{Identifiable, Post, PostWithComments}
import posting.domain.DomainJson._
import posting.domain.ProcessingError
import posting.domain.ProcessingError.ErrorOr
import posting.domain.ProcessingError.ProcessingErrorType.FileSystemError

private[posting] trait PostStorage[F[_]] {
  def storePosts(posts: List[Post]): F[ErrorOr[Unit]]
  def storePostsWithComments(posts: List[PostWithComments]): F[ErrorOr[Unit]]
}

private[posting] object PostStorage {
  def of[F[_]: Async](config: PostConfig): F[PostStorage[F]] =
    Slf4jLogger
      .create[F]
      .map(logger =>
        new PostStorage[F] {
          private val directory = File(config.storage.directory)

          override def storePosts(posts: List[Post]): F[ErrorOr[Unit]] = storeItems[Post](posts)

          override def storePostsWithComments(posts: List[PostWithComments]): F[ErrorOr[Unit]] =
            storeItems[PostWithComments](posts)

          private def storeItems[Item <: Identifiable: Encoder](posts: List[Item]): F[ErrorOr[Unit]] = {
            Stream
              .emits(posts)
              .covary[F]
              .parEvalMapUnordered(config.storage.maxConcurrentStores)(item =>
                Async[F].blocking {
                  File(directory.pathAsString, s"${item.id.toString}.json")
                    .createFileIfNotExists(createParents = true)
                    .write(Printer.spaces2.print(item.asJson))
                }.void,
              )
              .compile
              .drain
              .attemptT
              .leftSemiflatMap(handleError)
              .value
          }

          private def handleError(error: Throwable) =
            for {
              _ <- logger.error(error)("Cannot store a post")
              _ <- Async[F].blocking(if (directory.exists) directory.clear() else directory).void
            } yield ProcessingError(FileSystemError, error.getMessage)

        },
      )
}
