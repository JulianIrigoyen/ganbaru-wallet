package rest.wallets

import akka.cluster.sharding.typed.scaladsl.EntityRef
import akka.util.Timeout
import com.google.inject.Inject
import model.WalletFactory
import model.util.Acknowledge
import model.wallets.Wallet.WalletConfirmation
import model.wallets.{GandaruClientId, WalletCommands, WalletId}
import org.nullvector.api.json.JsonMapper
import play.Module.WalletsSystem
import play.api.libs.json.{util => _, _}
import play.api.mvc.{Action, ActionBuilder, AnyContent, InjectedController}
import sharding.EntityProvider

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt

object WalletResource {

  implicit val configuration = rest.defaultJsonConfiguration
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

  def hello() = Action {
    Ok("hello world")
  }

  def get() = Action {
    Ok("Gotten")
  }


  /** Implemented for TDD */
  def test(): Action[TestPost] = Action.async(parse.json[TestPost]) { request =>
    println("RECEIVED POST")
    println(request.body)
    Future.successful(Ok)
  }

  def confirmWallet(): Action[WalletConfirmationPost] = Action.async(parse.json[WalletConfirmationPost]) { request =>
    println(s"Received request to create wallet ${request.body}")
    val confirmation = request.body
    val clientId = GandaruClientId(confirmation.id)
    walletFactory.entityFor(clientId)
      .ask[Acknowledge[WalletId]](replyTo => WalletFactory.ConfirmWallet(WalletConfirmation(confirmation.cuit), replyTo))
      .transform(_.flatMap(_.toTry))
      .map(walletId => Created(Json.obj("wallet_id" -> walletId.walletId)))
  }

}

