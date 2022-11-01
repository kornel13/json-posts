package posting.logic

import cats.effect.IO
import cats.syntax.either._
import org.scalamock.scalatest.MockFactory
import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import posting.domain.Domain.{Comment, Post, PostWithComments}
import posting.domain.Ids._
import posting.domain.ProcessingError.ProcessingErrorType
import posting.domain.{EmailAddress, ProcessingError}
import posting.loggerStub

class PostProcessorSpec extends AnyFreeSpec with Matchers with MockFactory with EitherValues {
  import posting.logic.PostProcessorSpec._
  implicit val runtime = cats.effect.unsafe.IORuntime.global

  "PostProcessor" - {
    "must process posts" in new Fixture {
      (postReader.getPosts _)
        .expects()
        .returning(validPosts.asRightIO[ProcessingError])
        .once()

      (postStorage.storePosts _)
        .expects(validPosts)
        .returning(().asRightIO[ProcessingError])
        .once()

      val result = postProcessor.savePosts().unsafeRunSync()
      result.value mustEqual PostProcessor.postSavingSuccessful
    }

    "must process posts with comments" in new Fixture {
      (postReader.getPosts _)
        .expects()
        .returning(validPosts.asRightIO[ProcessingError])
        .once()

      (postReader.getComments _)
        .expects()
        .returning(validComments.asRightIO[ProcessingError])
        .once()

      (postStorage.storePostsWithComments _)
        .expects(expectedPostWithComments)
        .returning(().asRightIO[ProcessingError])
        .once()

      val result = postProcessor.savePostsWithComments().unsafeRunSync()
      result.value mustEqual PostProcessor.postWithCommentsSavingSuccessful
    }

    "must fail on invalid posts" in new Fixture {
      (postReader.getPosts _)
        .expects()
        .returning(invalidPosts.asRightIO[ProcessingError])
        .once()

      val result = postProcessor.savePosts().unsafeRunSync()
      result mustBe a[Left[_, _]]
      result.swap.value.title mustEqual ProcessingErrorType.InvalidDataFromServer

    }
    "must fail on invalid posts with comments" in new Fixture {
      (postReader.getPosts _)
        .expects()
        .returning(validPosts.asRightIO[ProcessingError])
        .once()

      (postReader.getComments _)
        .expects()
        .returning(invalidComments.asRightIO[ProcessingError])
        .once()

      val result = postProcessor.savePostsWithComments().unsafeRunSync()
      result mustBe a[Left[_, _]]
      result.swap.value.title mustEqual ProcessingErrorType.InvalidDataFromServer
    }

    "must fail on reader error" in new Fixture {
      (postReader.getPosts _)
        .expects()
        .returning(ProcessingError(ProcessingErrorType.DecodingError, "").asLeftIO[List[Post]])
        .once()

      (postStorage.storePosts _)
        .expects(validPosts)
        .returning(().asRightIO[ProcessingError])
        .never()

      val result = postProcessor.savePosts().unsafeRunSync()
      result mustBe a[Left[_, _]]
      result.swap.value.title mustEqual ProcessingErrorType.DecodingError
    }

    "must fail on storage error" in new Fixture {
      (postReader.getPosts _)
        .expects()
        .returning(validPosts.asRightIO[ProcessingError])
        .once()

      (postStorage.storePosts _)
        .expects(validPosts)
        .returning(ProcessingError(ProcessingErrorType.FileSystemError, "").asLeftIO[Unit])
        .once()

      val result = postProcessor.savePosts().unsafeRunSync()
      result mustBe a[Left[_, _]]
      result.swap.value.title mustEqual ProcessingErrorType.FileSystemError

    }
  }

  trait Fixture {
    val postReader    = mock[PostReader[IO]]
    val postStorage   = mock[PostStorage[IO]]
    val postProcessor = new PostProcessor.Impl[IO](postReader, postStorage, loggerStub[IO])
  }

  implicit final class BoxValueWithContext[A](private val value: A) {
    def asLeftIO[R]: IO[Either[A, R]]  = IO.pure(value.asLeft[R])
    def asRightIO[L]: IO[Either[L, A]] = IO.pure(value.asRight[L])
  }
}

object PostProcessorSpec {
  val validPosts: List[Post] = List(
    Post(
      UserId(1),
      PostId(1),
      "sunt aut facere repellat provident occaecati excepturi optio reprehenderit",
      "quia et suscipit suscipit recusandae consequuntur expedita et cum",
    ),
    Post(
      UserId(1),
      PostId(2),
      "qui est esse",
      "est rerum tempore vitae sequi sint nihil reprehenderit dolor beatae ea dolores neque",
    ),
    Post(
      UserId(1),
      PostId(3),
      "ea molestias quasi exercitationem repellat qui ipsa sit aut",
      "et iusto sed quo iure molestiae porro eius odio et labore et velit",
    ),
  )

  val validComments: List[Comment] = List(
    Comment(
      PostId(1),
      CommentId(1),
      "id labore ex et quam laborum",
      EmailAddress.parse("Eliseo@gardner.biz").get,
      "laudantium enim quasi est quidem magnam voluptate ipsam eos tempora quo necessitatibus",
    ),
    Comment(
      PostId(1),
      CommentId(2),
      "quo vero reiciendis velit similique earum",
      EmailAddress.parse("Jayne_Kuhic@sydney.com").get,
      "est natus enim nihil est dolore omnis voluptatem numquam et omnis occaecati quod ullam at",
    ),
    Comment(
      PostId(3),
      CommentId(3),
      "odio adipisci rerum aut animi",
      EmailAddress.parse("Nikita@garfield.biz").get,
      "quia molestiae reprehenderit quasi aspernatur aut expedita occaecati aliquam eveniet laudantium",
    ),
  )

  val expectedPostWithComments: List[PostWithComments] = List(
    PostWithComments(post = validPosts.head, comments = validComments.take(2)),
    PostWithComments(post = validPosts(1), comments = Seq.empty),
    PostWithComments(post = validPosts.last, comments = Seq(validComments.last)),
  )

  val invalidPosts: List[Post] = validPosts.map(post => if (post.id == PostId(2)) post.copy(id = PostId(1)) else post)

  val invalidComments: List[Comment] =
    validComments.map(comm => if (comm.id == CommentId(2)) comm.copy(id = CommentId(1)) else comm)
}
