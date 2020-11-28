// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/julian/Desktop/again/ganbaru-wallet/conf/routes
// @DATE:Sat Nov 28 06:43:23 ART 2020

package router

import play.core.routing._
import play.core.routing.HandlerInvokerFactory._

import play.api.mvc._

import _root_.controllers.Assets.Asset

class Routes(
  override val errorHandler: play.api.http.HttpErrorHandler, 
  // @LINE:1
  WalletResource_0: rest.wallets.WalletResource,
  // @LINE:4
  rest_wallets_WalletRouter_0: rest.wallets.WalletRouter,
  val prefix: String
) extends GeneratedRouter {

   @javax.inject.Inject()
   def this(errorHandler: play.api.http.HttpErrorHandler,
    // @LINE:1
    WalletResource_0: rest.wallets.WalletResource,
    // @LINE:4
    rest_wallets_WalletRouter_0: rest.wallets.WalletRouter
  ) = this(errorHandler, WalletResource_0, rest_wallets_WalletRouter_0, "/")

  def withPrefix(addPrefix: String): Routes = {
    val prefix = play.api.routing.Router.concatPrefix(addPrefix, this.prefix)
    router.RoutesPrefix.setPrefix(prefix)
    new Routes(errorHandler, WalletResource_0, rest_wallets_WalletRouter_0, prefix)
  }

  private[this] val defaultPrefix: String = {
    if (this.prefix.endsWith("/")) "" else "/"
  }

  def documentation = List(
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """hello""", """rest.wallets.WalletResource.hello"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """confirm""", """rest.wallets.WalletResource.confirmWallet()"""),
    prefixed_rest_wallets_WalletRouter_0_2.router.documentation,
    Nil
  ).foldLeft(List.empty[(String,String,String)]) { (s,e) => e.asInstanceOf[Any] match {
    case r @ (_,_,_) => s :+ r.asInstanceOf[(String,String,String)]
    case l => s ++ l.asInstanceOf[List[(String,String,String)]]
  }}


  // @LINE:1
  private[this] lazy val rest_wallets_WalletResource_hello0_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("hello")))
  )
  private[this] lazy val rest_wallets_WalletResource_hello0_invoker = createInvoker(
    WalletResource_0.hello,
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "rest.wallets.WalletResource",
      "hello",
      Nil,
      "GET",
      this.prefix + """hello""",
      """""",
      Seq()
    )
  )

  // @LINE:2
  private[this] lazy val rest_wallets_WalletResource_confirmWallet1_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("confirm")))
  )
  private[this] lazy val rest_wallets_WalletResource_confirmWallet1_invoker = createInvoker(
    WalletResource_0.confirmWallet(),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "rest.wallets.WalletResource",
      "confirmWallet",
      Nil,
      "POST",
      this.prefix + """confirm""",
      """""",
      Seq()
    )
  )

  // @LINE:4
  private[this] val prefixed_rest_wallets_WalletRouter_0_2 = Include(rest_wallets_WalletRouter_0.withPrefix(this.prefix + (if (this.prefix.endsWith("/")) "" else "/") + "api"))


  def routes: PartialFunction[RequestHeader, Handler] = {
  
    // @LINE:1
    case rest_wallets_WalletResource_hello0_route(params@_) =>
      call { 
        rest_wallets_WalletResource_hello0_invoker.call(WalletResource_0.hello)
      }
  
    // @LINE:2
    case rest_wallets_WalletResource_confirmWallet1_route(params@_) =>
      call { 
        rest_wallets_WalletResource_confirmWallet1_invoker.call(WalletResource_0.confirmWallet())
      }
  
    // @LINE:4
    case prefixed_rest_wallets_WalletRouter_0_2(handler) => handler
  }
}
