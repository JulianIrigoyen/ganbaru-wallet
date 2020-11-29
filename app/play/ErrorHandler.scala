package play

import play.api.http.DefaultHttpErrorHandler
import play.api.http.Status.{BAD_REQUEST, FORBIDDEN, NOT_FOUND}
import play.api.mvc.{RequestHeader, Result, Results}
import play.api.mvc.Results.{BadRequest, InternalServerError}

import scala.concurrent.Future

/**
 * Provides a stripped down error handler that does not use HTML in error pages, and
 * prints out debugging output.
 *
 * https://www.playframework.com/documentation/latest/ScalaErrorHandling
 */
class ErrorHandler extends DefaultHttpErrorHandler {
  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {

    Future.successful {
      val result = statusCode match {
        case BAD_REQUEST => Results.BadRequest(message)
        case FORBIDDEN => Results.Forbidden(message)
        case NOT_FOUND => Results.NotFound(message)
        case clientError if statusCode >= 400 && statusCode < 500 => Results.Status(statusCode)
        case nonClientError =>
          val msg = s"onClientError invoked with non client error status code $statusCode: $message"
          throw new IllegalArgumentException(msg)
      }
      result
    }
  }

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    exception.printStackTrace()
    exception match {
      case e: IllegalArgumentException => Future.successful(BadRequest(e.toString))
      case e => Future.successful(InternalServerError(e.toString))
    }
  }
}
