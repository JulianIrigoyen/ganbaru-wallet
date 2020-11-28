package rest.wallets

import akka.cluster.sharding.typed.scaladsl.EntityRef
import akka.util.Timeout
import com.google.inject.Inject
import model.{GandaruClientId, WalletFactory, WalletId}
import model.wallets.WalletCommands
import org.nullvector.api.json.JsonMapper
import play.Module.WalletsSystem
import play.api.libs.json.{util => _, _}
import play.api.mvc.{Action, InjectedController}
import sharding.EntityProvider

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt

object WalletResource {

  implicit val configuration = rest.defaultJsonConfiguration
  implicit val wcpr = JsonMapper.readsOf[WalletConfirmationPost]
  implicit val wcpw = JsonMapper.writesOf[WalletConfirmationPost]

  case class WalletConfirmationPost(cuit: String)

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

  def confirmWallet(): Action[WalletConfirmationPost] = Action.async(parse.json[WalletConfirmationPost]) { request =>
    val cuit = request.body
    val x: EntityRef[WalletFactory.Command] = walletFactory.entityFor(GandaruClientId(1))
    Future.successful(Ok(cuit.asJson))

  }
}
