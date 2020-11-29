package model.wallets

import java.time.LocalDateTime

import model.AccountType.AccountType
import model.{Account, AccountId, Money, TransactionId}
import model.wallets.Wallet.WalletConfirmation

/** This interface defines all the events that the Wallet persistent actor will write to the LevelDB journal . */

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

  final case class Deposited(
                            walletId: WalletId,
                            gandaruClientId: GandaruClientId,
                            account: Account,
                            amount: Money,
                            timestamp: LocalDateTime
                            ) extends Event

  final case class Withdrew(
                           walletId: WalletId,
                           gandaruClientId: GandaruClientId,
                           account: Account,
                           amount: Money,
                           timestamp: LocalDateTime
                           ) extends Event

  final case class TransactionValidated(
                                       walletId: WalletId,
                                       gandaruClientId: GandaruClientId,
                                       transactionId: TransactionId,
                                       debited: Account,
                                       credited: Account,
                                       amount: Money,
                                       timestamp: LocalDateTime
                                       ) extends Event

  final case class TransactionRolledback(
                                          walletId: WalletId,
                                          gandaruClientId: GandaruClientId,
                                          transactionId: TransactionId,
                                          accountToDebit: Account,
                                          accountToCredit: Account,
                                          amount: Money,
                                          timestamp: LocalDateTime
                                        ) extends Event

}
