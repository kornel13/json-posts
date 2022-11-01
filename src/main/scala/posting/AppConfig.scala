package posting

import cats.effect.Sync
import cats.syntax.either._
import org.http4s.Uri
import org.http4s.Uri.Path
import pureconfig.error.CannotConvert
import pureconfig.generic.auto._
import pureconfig.{ConfigReader, ConfigSource}
import posting.http.HttpConfig
import posting.logic.PostConfig

import scala.util.Try

private[posting] final case class AppConfig(http: HttpConfig, post: PostConfig)

private[posting] object AppConfig {
  implicit val uriReader: ConfigReader[Uri] = ConfigReader.fromNonEmptyString(string =>
    Uri.fromString(string).leftMap(error => CannotConvert(string, "Uri", error.getMessage)),
  )
  implicit val pathReader: ConfigReader[Path] = ConfigReader.fromNonEmptyString(string =>
    Try(Path.unsafeFromString(string)).toEither
      .leftMap(error => CannotConvert(string, "Path", error.getMessage)),
  )

  def load[F[_]: Sync]: F[AppConfig] = Sync[F].delay(ConfigSource.default.at("placeholder-post").loadOrThrow[AppConfig])
}
