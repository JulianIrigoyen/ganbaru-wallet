package rest.wallets

import javax.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class WalletRouter @Inject()(resource: WalletResource) extends SimpleRouter {
  override def routes: Routes = {
    case POST(p"") => ???
  }
}
