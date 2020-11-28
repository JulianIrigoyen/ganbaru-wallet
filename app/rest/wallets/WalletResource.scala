package rest.wallets

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.Done
import akka.cluster.sharding.typed.scaladsl.EntityRef
import akka.util.Timeout
import com.google.inject.Inject
import model.WalletFactory
import model.util.{Acknowledge, AcknowledgeWithFailure, AcknowledgeWithResult}
import model.wallets.Wallet.WalletConfirmation
import model.wallets.WalletCommands.GetWallet
import model.wallets.WalletEvents.WalletCreated
import model.wallets.{CreatedWallet, GandaruClientId, WalletCommands, WalletId}
import org.nullvector.api.json.JsonMapper
import play.Module.WalletsSystem
import play.api.libs.json.{util => _, _}
import play.api.mvc.{Action, ActionBuilder, AnyContent, InjectedController, Result}
import sharding.EntityProvider

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt

object WalletResource {

  implicit val configuration = rest.defaultJsonConfiguration
  implicit val cw = JsonMapper.writesOf[CreatedWallet]
  implicit val wcpr = JsonMapper.readsOf[WalletConfirmationPost]
  implicit val wcpw = JsonMapper.writesOf[WalletConfirmationPost]
  implicit val tpr = JsonMapper.readsOf[TestPost]
  implicit val tpw = JsonMapper.writesOf[TestPost]

  case class WalletConfirmationPost(
                                     id: String,
                                     cuit: String
                                   )

  case class TestPost(id: Int, cuit: String)

}


class WalletResource @Inject()(
                                walletsSystem: WalletsSystem,
                                walletProvider:  EntityProvider[WalletCommands.Command, WalletId],
                                walletFactory: EntityProvider[WalletFactory.Command, GandaruClientId]
                              )
                              (implicit ex: ExecutionContext) extends InjectedController {

  import org.nullvector.api.json.JsonMapper._
  import rest.wallets.WalletResource._

  private implicit val timeout: Timeout = Timeout(30.seconds)

  /** Implemented for TDD */
  def test(): Action[TestPost] = Action.async(parse.json[TestPost]) { request =>
    println("RECEIVED POST")
    println(request.body)
    Future.successful(Created)
  }

  def confirmWallet(): Action[WalletConfirmationPost] = Action.async(parse.json[WalletConfirmationPost]) { request =>
    println(s"Received request to create wallet ${request.body}")
    val confirmation = request.body
    val clientId = GandaruClientId(confirmation.id)
    walletFactory.entityFor(clientId)
      .ask[Acknowledge[WalletId]](replyTo => WalletFactory.ConfirmWallet(WalletConfirmation(confirmation.cuit), replyTo))
      .transform(_.flatMap(_.toTry))
      .map(walletId => Created(Json.obj("wallet_id" -> walletId.id)))
  }

  def getWallet(walletId: WalletId): Action[AnyContent] = Action.async { _ =>
    println(s"Received get request for wallet $walletId")
    walletProvider.entityFor(walletId)
      .ask[Acknowledge[CreatedWallet]](replyTo => GetWallet(replyTo))
      .map(acknowledgement => toResult[CreatedWallet](acknowledgement, Ok(_)) )
  }


  private def acknowledgementToResult (answer: Acknowledge[Done]) = {
    answer match {
      case AcknowledgeWithResult(result) =>  Ok
      case AcknowledgeWithFailure(throwable) =>
        println("Ack Failed")
        Ok
    }
  }

  private def toResult[T](acknowledgement: Acknowledge[T], onSuccess: JsValue => Result )(implicit  w: Writes[T]) = {
    acknowledgement match {
      case AcknowledgeWithResult(result: T) => onSuccess(result.asJson)
      case AcknowledgeWithFailure(throwable) =>
        println("Typed Ack Failed")
        Ok
    }
  }

/*  private def handleUnsuccessfulAnswer[T](answer: Answer[T]) = {
    answer match {
      case NoAnswer(reason) => NotFoundResult(reason)
      case FailureAnswer(throwable) => ServerErrorResult(throwable)
      case ErrorCodeAnswer(errorCode, errorMessage) => BadRequestResult(new IllegalStateException(errorCode + " : " + errorMessage))
    }
  }
*/
}

