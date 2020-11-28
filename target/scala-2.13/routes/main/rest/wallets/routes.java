// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/julian/Desktop/again/ganbaru-wallet/conf/routes
// @DATE:Sat Nov 28 06:43:23 ART 2020

package rest.wallets;

import router.RoutesPrefix;

public class routes {
  
  public static final rest.wallets.ReverseWalletResource WalletResource = new rest.wallets.ReverseWalletResource(RoutesPrefix.byNamePrefix());

  public static class javascript {
    
    public static final rest.wallets.javascript.ReverseWalletResource WalletResource = new rest.wallets.javascript.ReverseWalletResource(RoutesPrefix.byNamePrefix());
  }

}
