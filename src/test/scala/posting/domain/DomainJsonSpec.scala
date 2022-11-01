package posting.domain

import io.circe.parser.parse
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import posting.domain.Domain._
import posting.domain.DomainJson._
import posting.domain.Ids._
import posting.loadText

class DomainJsonSpec extends AnyFreeSpec with Matchers with EitherValues {
  import DomainJsonSpec._

  "DomainJsonSpec" - {
    "must serialize and deserialize a list of posts" in testCodec[List[Post]]("posts.json")(expectedPosts)

    "must serialize and deserialize a list of comments" in testCodec[List[Comment]]("comments.json")(expectedComments)

    "must serialize and deserialize a post with comments" in testCodec[PostWithComments]("post-with-comments.json")(
      expectedPostWithComment,
    )
  }

  private def testCodec[A: Encoder: Decoder](path: String)(expected: A) = {
    val response = loadText(path)
    val (json, decoded) = {
      for {
        parsed  <- parse(response)
        decoded <- parsed.as[A]
      } yield (parsed, decoded)
    }.value

    decoded mustEqual expected
    expected.asJson mustEqual json
    expected.asJson.as[A].value mustEqual decoded
  }

  object DomainJsonSpec {
    val expectedPosts: List[Post] = List(
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
      Post(
        UserId(1),
        PostId(4),
        "eum et est occaecati",
        "ullam et saepe reiciendis voluptatem adipisci sit amet autem assumenda provident rerum culpa",
      ),
      Post(
        UserId(1),
        PostId(5),
        "nesciunt quas odio",
        "repudiandae veniam quaerat sunt sed alias aut fugiat sit autem sed est",
      ),
    )

    val expectedComments: List[Comment] = List(
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
        PostId(1),
        CommentId(3),
        "odio adipisci rerum aut animi",
        EmailAddress.parse("Nikita@garfield.biz").get,
        "quia molestiae reprehenderit quasi aspernatur aut expedita occaecati aliquam eveniet laudantium",
      ),
      Comment(
        PostId(1),
        CommentId(4),
        "alias odio sit",
        EmailAddress.parse("Lew@alysha.tv").get,
        "non et atque occaecati deserunt quas accusantium unde odit nobis qui voluptatem",
      ),
    )

    val expectedPostWithComment: PostWithComments = PostWithComments(
      post = Post(
        UserId(1),
        PostId(1),
        "sunt aut facere repellat provident occaecati excepturi optio reprehenderit",
        "quia et suscipit suscipit recusandae consequuntur expedita et cum",
      ),
      comments = Seq(
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
      ),
    )

  }
}
