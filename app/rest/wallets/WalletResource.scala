package rest.wallets

import akka.util.Timeout
import javax.inject.Inject
import play.api.mvc.InjectedController

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

object WalletResource {

}
class WalletResource @Inject()(

                              )
                              (implicit ex: ExecutionContext) extends InjectedController {

  import org.nullvector.api.json.JsonMapper._
  import rest.wallets.WalletResource._

  private implicit val timeout: Timeout = Timeout(30.seconds)



}
