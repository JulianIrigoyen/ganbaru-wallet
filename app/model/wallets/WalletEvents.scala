package model.wallets

import java.time.LocalDateTime

import akka.actor.typed.ActorSystem
import model.AccountType.AccountType
import model.{Account, AccountId, Money, TransactionId}
import model.wallets.Wallet.WalletConfirmation
import org.nullvector.{EventAdapter, EventAdapterFactory, ReactiveMongoEventSerializer}
import play.Module.WalletsSystem
import adapter.AdaptableEvents
import reactivemongo.api.bson.MacroConfiguration.Aux
import reactivemongo.api.bson.{BSONDocument, MacroConfiguration, MacroOptions, TypeNaming}

/** This interface defines all the events that the Wallet persistent actor will write to the LevelDB journal . */

object WalletEvents extends AdaptableEvents {

  sealed trait Event

  final case class WalletCreated(
                                  walletId: WalletId,
                                  ganbaruClientId: GanbaruClientId,
                                  walletNumber: WalletNumber,
                                  confirmation: WalletConfirmation,
                                  timestamp: LocalDateTime
                                ) extends Event

  final case class AccountAdded(
                                 walletId: WalletId,
                                 ganbaruClientId: GanbaruClientId,
                                 accountId: AccountId,
                                 cuit: String,
                                 accountType: AccountType,
                                 balance: Money,
                                 dateOpened: LocalDateTime
                               ) extends Event

  final case class Deposited(
                              walletId: WalletId,
                              ganbaruClientId: GanbaruClientId,
                              account: Account,
                              amount: Money,
                              timestamp: LocalDateTime
                            ) extends Event

  final case class Withdrew(
                             walletId: WalletId,
                             ganbaruClientId: GanbaruClientId,
                             account: Account,
                             amount: Money,
                             timestamp: LocalDateTime
                           ) extends Event

  final case class TransactionValidated(
                                         walletId: WalletId,
                                         ganbaruClientId: GanbaruClientId,
                                         transactionId: TransactionId,
                                         debited: Account,
                                         credited: Account,
                                         amount: Money,
                                         timestamp: LocalDateTime
                                       ) extends Event

  final case class TransactionRolledback(
                                          walletId: WalletId,
                                          ganbaruClientId: GanbaruClientId,
                                          transactionId: TransactionId,
                                          accountToDebit: Account,
                                          accountToCredit: Account,
                                          amount: Money,
                                          timestamp: LocalDateTime
                                        ) extends Event

  /**
   *  Before saving any event from your persistent actor,
   *  the corresponding EventAdapter needs to be registered
   *
  https://github.com/null-vector/akka-reactivemongo-plugin#events-adapters
   */

  override def adapt(): Seq[EventAdapter[_]] = Seq(
      EventAdapterFactory.adapt[WalletCreated]( "WalletCreated"),
      EventAdapterFactory.adapt[AccountAdded]("AccountAdded"),
      EventAdapterFactory.adapt[Deposited]("Deposited"),
      EventAdapterFactory.adapt[Withdrew]( "Withdrew"),
      EventAdapterFactory.adapt[TransactionValidated]( "TransactionValidated"),
      EventAdapterFactory.adapt[TransactionRolledback]( "TransactionRolledBack")
    )

}
