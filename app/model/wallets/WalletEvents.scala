package model.wallets

import java.time.LocalDateTime

import akka.stream.scaladsl.Balance
import model.AccountType.AccountType
import model.Money.Currency
import model.{Account, AccountId, Money}
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

  final case class AccountAdded(
                               walletId: WalletId,
                               gandaruClientId: GandaruClientId,
                               accountId: AccountId,
                               cuit: String,
                               accountType: AccountType,
                               balance: Money,
                               dateOpened: LocalDateTime
                               ) extends Event

}
