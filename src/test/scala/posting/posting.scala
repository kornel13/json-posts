import better.files._
import cats.Applicative
import org.typelevel.log4cats.Logger

package object posting {
  def loadText(filePath: String): String = Resource.getAsString(filePath)

  def loggerStub[F[_]: Applicative] = new Logger[F] {
    override def error(message: => String): F[Unit] = Applicative[F].unit
    override def warn(message: => String): F[Unit]  = Applicative[F].unit
    override def info(message: => String): F[Unit]  = Applicative[F].unit
    override def debug(message: => String): F[Unit] = Applicative[F].unit
    override def trace(message: => String): F[Unit] = Applicative[F].unit

    override def error(t: Throwable)(message: => String): F[Unit] = Applicative[F].unit
    override def warn(t: Throwable)(message: => String): F[Unit]  = Applicative[F].unit
    override def info(t: Throwable)(message: => String): F[Unit]  = Applicative[F].unit
    override def debug(t: Throwable)(message: => String): F[Unit] = Applicative[F].unit
    override def trace(t: Throwable)(message: => String): F[Unit] = Applicative[F].unit
  }
}
