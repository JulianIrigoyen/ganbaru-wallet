// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/julian/Desktop/again/ganbaru-wallet/conf/routes
// @DATE:Sat Nov 28 14:55:35 ART 2020

import play.api.routing.JavaScriptReverseRoute


import _root_.controllers.Assets.Asset

// @LINE:1
package rest.wallets.javascript {

  // @LINE:1
  class ReverseWalletResource(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:1
    def hello: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "rest.wallets.WalletResource.hello",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "hello"})
        }
      """
    )
  
    // @LINE:2
    def confirmWallet: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "rest.wallets.WalletResource.confirmWallet",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "confirm"})
        }
      """
    )
  
  }


}
