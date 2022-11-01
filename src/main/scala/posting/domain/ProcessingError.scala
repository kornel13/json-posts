package posting.domain

import enumeratum.{CirceEnum, Enum, EnumEntry}
import posting.domain.ProcessingError.ProcessingErrorType

private[posting] final case class ProcessingError(title: ProcessingErrorType, message: String) {
  override def toString: String = s"$title:\n$message"
  def httpBody: String          = s"$title\nNotify a maintainer of the product"
}

private[posting] object ProcessingError {
  sealed trait ProcessingErrorType extends EnumEntry
  object ProcessingErrorType extends Enum[ProcessingErrorType] with CirceEnum[ProcessingErrorType] {
    case object InvalidDataFromServer extends ProcessingErrorType
    case object DecodingError         extends ProcessingErrorType
    case object ServerConnectionError extends ProcessingErrorType
    case object FileSystemError       extends ProcessingErrorType

    override def values: IndexedSeq[ProcessingErrorType] = findValues
  }

  type ErrorOr[A] = Either[ProcessingError, A]
}
