package model.util

import scala.util.{Failure, Success, Try}

sealed trait Acknowledge[+T] {
  def toTry: Try[T]
  def toOption: Option[T]
  def get: T
}

case class AcknowledgeWithResult[+T](result: T) extends Acknowledge[T] {
  override def toTry: Try[T] = Success(result)
  override def toOption: Option[T] = Some(result)
  override def get: T = result
}

case class AcknowledgeWithFailure[T](errorMessage: String) extends Acknowledge[T] {
  override def toTry: Try[T] = Failure(new Exception(s"$errorMessage"))
  override def toOption: Option[T] = None
  override def get: T = toTry.get
}
