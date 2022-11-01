package posting.util

import cats.Applicative
import cats.data.EitherT

private[posting] object EitherTOps {

  implicit final class EitherTOps[F[_], Err, A](private val eitherF: F[Either[Err, A]]) extends AnyVal {
    def T: EitherT[F, Err, A] = EitherT(eitherF)
  }

  implicit final class EitherTEncapsulationOps[A](private val value: A) extends AnyVal {
    def asLeftT[F[_] : Applicative, R]: EitherT[F, A, R] = EitherT.leftT[F, R](value)

    def asRightT[F[_] : Applicative, L]: EitherT[F, L, A] = EitherT.rightT[F, L](value)
  }
}
