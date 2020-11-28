// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/julian/Desktop/again/ganbaru-wallet/conf/routes
// @DATE:Sat Nov 28 16:11:59 ART 2020

package router

import play.core.routing._
import play.core.routing.HandlerInvokerFactory._

import play.api.mvc._

import _root_.controllers.Assets.Asset

class Routes(
  override val errorHandler: play.api.http.HttpErrorHandler, 
  // @LINE:1
  rest_wallets_WalletRouter_0: rest.wallets.WalletRouter,
  val prefix: String
) extends GeneratedRouter {

   @javax.inject.Inject()
   def this(errorHandler: play.api.http.HttpErrorHandler,
    // @LINE:1
    rest_wallets_WalletRouter_0: rest.wallets.WalletRouter
  ) = this(errorHandler, rest_wallets_WalletRouter_0, "/")

  def withPrefix(addPrefix: String): Routes = {
    val prefix = play.api.routing.Router.concatPrefix(addPrefix, this.prefix)
    router.RoutesPrefix.setPrefix(prefix)
    new Routes(errorHandler, rest_wallets_WalletRouter_0, prefix)
  }

  private[this] val defaultPrefix: String = {
    if (this.prefix.endsWith("/")) "" else "/"
  }

  def documentation = List(
    prefixed_rest_wallets_WalletRouter_0_0.router.documentation,
    Nil
  ).foldLeft(List.empty[(String,String,String)]) { (s,e) => e.asInstanceOf[Any] match {
    case r @ (_,_,_) => s :+ r.asInstanceOf[(String,String,String)]
    case l => s ++ l.asInstanceOf[List[(String,String,String)]]
  }}


  // @LINE:1
  private[this] val prefixed_rest_wallets_WalletRouter_0_0 = Include(rest_wallets_WalletRouter_0.withPrefix(this.prefix + (if (this.prefix.endsWith("/")) "" else "/") + "api/wallets"))


  def routes: PartialFunction[RequestHeader, Handler] = {
  
    // @LINE:1
    case prefixed_rest_wallets_WalletRouter_0_0(handler) => handler
  }
}
