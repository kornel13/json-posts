package posting.domain

import cats.syntax.option._
import scala.util.matching.Regex

case class EmailAddress private (address: String) extends AnyVal {
  override def toString: String = address
}

object EmailAddress {
  val emailRegex: Regex = """^[\w-.]+@([\w-]+\.)+[\w-]{2,4}$""".r

  def parse(address: String): Option[EmailAddress] = address match {
    case correctAddress @ emailRegex(_) => EmailAddress(correctAddress).some
    case _                              => None
  }
}
