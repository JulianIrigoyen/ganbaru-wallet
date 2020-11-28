// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/julian/Desktop/again/ganbaru-wallet/conf/routes
// @DATE:Sat Nov 28 14:55:35 ART 2020

import play.api.mvc.Call


import _root_.controllers.Assets.Asset

// @LINE:1
package rest.wallets {

  // @LINE:1
  class ReverseWalletResource(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:1
    def hello(): Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "hello")
    }
  
    // @LINE:2
    def confirmWallet(): Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "confirm")
    }
  
  }


}
