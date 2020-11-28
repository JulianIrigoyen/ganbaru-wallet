package rest.wallets

import javax.inject.{Inject, Named}
import play.Module.WalletsSystem
import model._
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._
import play.api.mvc.Action

class WalletRouter @Inject() (resource: WalletResource) extends SimpleRouter {

  override def routes: Routes = {
    case POST(p"") => resource.hello
  }
}
