package posting.domain

private[posting] object Ids {
  final case class UserId(id: Int) extends AnyVal {
    override def toString: String = id.toString
  }

  final case class PostId(id: Int) extends AnyVal {
    override def toString: String = id.toString
  }

  final case class CommentId(id: Int) extends AnyVal {
    override def toString: String = id.toString
  }
}
