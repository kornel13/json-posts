package posting.domain

import io.circe.generic.extras.semiauto.{deriveUnwrappedDecoder, deriveUnwrappedEncoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import posting.domain.Domain._
import posting.domain.Ids._

private[posting] object DomainJson {

  private implicit val userIdEncoder: Encoder[UserId] = deriveUnwrappedEncoder
  private implicit val userIdDecoder: Decoder[UserId] = deriveUnwrappedDecoder

  private implicit val postIdEncoder: Encoder[PostId] = deriveUnwrappedEncoder
  private implicit val postIdDecoder: Decoder[PostId] = deriveUnwrappedDecoder

  private implicit val commentIdEncoder: Encoder[CommentId] = deriveUnwrappedEncoder
  private implicit val commentIdDecoder: Decoder[CommentId] = deriveUnwrappedDecoder

  private implicit val emailEncoder: Encoder[EmailAddress] = Encoder.encodeString.contramap(_.address)
  private implicit val emailDecoder: Decoder[EmailAddress] =
    Decoder.decodeString.emap(EmailAddress.parse(_).toRight("Invalid email"))

  implicit val postEncoder: Encoder[Post] = deriveEncoder
  implicit val postDecoder: Decoder[Post] = deriveDecoder

  implicit val commentEncoder: Encoder[Comment] = deriveEncoder
  implicit val commentDecoder: Decoder[Comment] = deriveDecoder

  implicit val postWithCommentsEncoder: Encoder[PostWithComments] = deriveEncoder
  implicit val postWithCommentsDecoder: Decoder[PostWithComments] = deriveDecoder
}
