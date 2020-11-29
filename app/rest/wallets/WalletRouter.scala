package rest.wallets

import javax.inject.{Inject, Named}
import play.Module.WalletsSystem
import model._
import model.wallets.WalletId
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._
import play.api.mvc.Action

class WalletRouter @Inject() (resource: WalletResource) extends SimpleRouter {

  override def routes: Routes = {

    case POST(p"/test") => resource.test()

    case POST(p"/confirm") => resource.confirmWallet()
    case GET(p"/$walletId") => resource.getWallet(WalletId(walletId))

    case POST(p"/$walletId/account") => resource.addAccount(WalletId(walletId))
    case GET(p"/$walletId/accounts/$accountId") => resource.getAccount(WalletId(walletId), AccountId(accountId))
    case GET(p"/$walletId/bulkiest") => resource.getBulkiestAccount(WalletId(walletId))

    case PATCH(p"/$walletId/accounts/$accountId/deposit") => resource.deposit(WalletId(walletId), AccountId(accountId))
    case PATCH(p"/$walletId/accounts/$accountId/withdraw") => resource.withdraw(WalletId(walletId), AccountId(accountId))

    case POST(p"/$walletId/transfer") => resource.transfer(WalletId(walletId))

  }
}
