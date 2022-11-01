package posting.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

private[posting] object ProcessingErrorJson {

  implicit val commentIdEncoder: Encoder[ProcessingError] = deriveEncoder
  implicit val commentIdDecoder: Decoder[ProcessingError] = deriveDecoder

}
