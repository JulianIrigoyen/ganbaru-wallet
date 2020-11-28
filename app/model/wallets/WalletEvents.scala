package model.wallets

import model.{Account, WalletId}

object WalletEvents {

  sealed trait Event
  final case class WalletCreated(walletId: WalletId) extends Event
  final case class AccountAdded(account: Account) extends Event

}
