package model.wallets

import java.time.LocalDateTime

import model.Account
import model.wallets.Wallet.WalletConfirmation


object WalletEvents {

  sealed trait Event
  final case class WalletCreated(
                                  walletId: WalletId,
                                  gandaruClientId: GandaruClientId,
                                  walletNumber: WalletNumber,
                                  confirmation: WalletConfirmation,
                                  timestamp: LocalDateTime
                                ) extends Event

  final case class AccountAdded(account: Account) extends Event

}
