package posting.domain

import posting.domain.Ids._

private[posting] object Domain {
  trait Identifiable {
    type Id
    def id: Id
  }
  final case class Post(userId: UserId, id: PostId, title: String, body: String) extends Identifiable {
    override type Id = PostId
  }

  final case class Comment(postId: PostId, id: CommentId, name: String, email: EmailAddress, body: String)
      extends Identifiable {
    override type Id = CommentId
  }

  final case class PostWithComments(post: Post, comments: Seq[Comment]) extends Identifiable {
    override type Id = PostId
    override def id: PostId = post.id
  }

}
