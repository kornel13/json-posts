package posting.logic

import cats.NonEmptyParallel
import cats.data.ValidatedNec
import cats.effect.kernel.Async
import cats.syntax.apply._
import cats.syntax.either._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.parallel._
import cats.syntax.validated._
import org.http4s.client.Client
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import posting.domain
import posting.domain.Domain._
import posting.domain.ProcessingError
import posting.domain.ProcessingError.ErrorOr
import posting.domain.ProcessingError.ProcessingErrorType.InvalidDataFromServer
import posting.util.EitherTOps._

private[posting] trait PostProcessor[F[_]] {
  def savePosts(): F[ErrorOr[String]]
  def savePostsWithComments(): F[ErrorOr[String]]
}

private[posting] object PostProcessor {
  def of[F[_]: Async: NonEmptyParallel](config: PostConfig, client: Client[F]): F[PostProcessor[F]] = {
    for {
      reader  <- PostReader.of(config, client)
      storage <- PostStorage.of(config)
      logger  <- Slf4jLogger.create[F]
    } yield new Impl[F](reader, storage, logger)
  }

  val postSavingSuccessful             = "Stored posts without comments"
  val postWithCommentsSavingSuccessful = "Stored posts with comments"

  class Impl[F[_]: Async: NonEmptyParallel](reader: PostReader[F], storage: PostStorage[F], logger: Logger[F])
      extends PostProcessor[F] {
    override def savePosts(): F[ErrorOr[String]] =
      (for {
        posts <- reader.getPosts.T
        validatedPosts <- validateUniqueness(posts, "Post").toEither
                           .leftMap(err => ProcessingError(InvalidDataFromServer, err.reduce))
                           .toEitherT
        _ <- storage.storePosts(validatedPosts).T
        _ <- logger.debug(postSavingSuccessful).map(_.asRight[domain.ProcessingError]).T
      } yield postSavingSuccessful)
        .leftSemiflatTap(error => logger.error(s"Failed to process posts: $error"))
        .value

    override def savePostsWithComments(): F[ErrorOr[String]] =
      getPostAndComments.flatMap(postAndCommentsT =>
        (for {
          (post, comments) <- postAndCommentsT
          postWithComments = groupPostWithComments(post, comments)
          _                <- storage.storePostsWithComments(postWithComments).T
          _                <- logger.debug(postWithCommentsSavingSuccessful).map(_.asRight[domain.ProcessingError]).T
        } yield postWithCommentsSavingSuccessful)
          .leftSemiflatTap(error => logger.error(s"Failed to process posts: $error"))
          .value,
      )

    private def getPostAndComments =
      (reader.getPosts, reader.getComments).parMapN {
        case (Right(posts), Right(comments)) =>
          (validateUniqueness(posts, "Post"), validateUniqueness(comments, "Comment")).tupled.toEither
            .leftMap(err => ProcessingError(InvalidDataFromServer, err.reduce))
            .toEitherT
        case (Left(err), _) => err.asLeftT[F, (List[Post], List[Comment])]
        case (_, Left(err)) => err.asLeftT[F, (List[Post], List[Comment])]
      }

    private def groupPostWithComments(posts: List[Post], comments: List[Comment]): List[PostWithComments] = {
      val commentsMap = comments.groupBy(_.postId)
      posts.map(post => PostWithComments(post, commentsMap.getOrElse(post.id, Seq.empty)))
    }

    private def validateUniqueness[Item <: Identifiable](
      items: List[Item],
      itemLabel: String,
    ): ValidatedNec[String, List[Item]] =
      if (items.distinctBy(_.id).length != items.length)
        s"Ids of $itemLabel are not unique".invalidNec
      else items.validNec
  }
}
