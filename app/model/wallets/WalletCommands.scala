package model.wallets

import java.time.LocalDateTime

import akka.actor.typed.ActorRef
import model.Money.Currency
import model.util.Acknowledge
import model.wallets.Wallet.WalletConfirmation
import model.wallets.WalletEvents.{AccountAdded, Deposited, TransactionRolledback, TransactionValidated, WalletCreated, Withdrew}
import model.{Account, AccountId, AccountType, Money, Transaction, TransactionId}

/** This interface defines all the commands that the Wallet persistent actor supports. */

object WalletCommands {

  sealed trait Command

  final case class CreateWalletWithNumber(
                                 gandaruClientId: GandaruClientId,
                                 walletNumber: WalletNumber,
                                 confirmation: WalletConfirmation,
                                 replyTo: ActorRef[Acknowledge[WalletId]])
                                extends Command {
    def asEvent(walletId: WalletId): WalletCreated = {
      WalletCreated(walletId, gandaruClientId, walletNumber, confirmation, LocalDateTime.now())
    }
  }

  final case class GetWallet(replyTo: ActorRef[Acknowledge[CreatedWallet]])
    extends Command

  final case class AddAccount(
                               cuit: String,
                               accountType: AccountType.AccountType,
                               currency: Currency,
                               replyTo: ActorRef[Acknowledge[AccountId]])
                              extends Command {
    def asEvent(wallet: CreatedWallet, accountId: AccountId): AccountAdded = {
      AccountAdded(
        wallet.walletId,
        wallet.gandaruClientId,
        accountId,
        cuit,
        accountType,
        Money(0, currency),
        LocalDateTime.now()
      )
    }
  }

  final case class GetAccount(accountId: AccountId, replyTo: ActorRef[Acknowledge[Account]])
    extends Command

  final case class GetBulkiestAccount(replyTo: ActorRef[Acknowledge[Account]])
    extends Command

  final case class Deposit(
                            accountId: AccountId,
                            amount: Money,
                            replyTo: ActorRef[Acknowledge[Account]]
                          ) extends Command {
    def asEvent(wallet: CreatedWallet, depositTo: Account): Deposited = {
      Deposited(wallet.walletId, wallet.gandaruClientId, depositTo, amount, LocalDateTime.now())
    }
  }

  final case class Withdraw(
                             accountId: AccountId,
                             amount: Money,
                             replyTo: ActorRef[Acknowledge[Account]]
                           ) extends Command {
    def asEvent(wallet: CreatedWallet, withdrawFrom: Account): Withdrew = {
      Withdrew(wallet.walletId, wallet.gandaruClientId, withdrawFrom, amount,  LocalDateTime.now())
    }
  }

  final case class AttemptTransaction(
                                       debit: AccountId,
                                       credit: AccountId,
                                       amount: Money,
                                       replyTo: ActorRef[Acknowledge[TransactionId]]
                                     ) extends Command {
    def asEvent(wallet: CreatedWallet, tranasctionId: TransactionId, debit: Account, credit: Account): TransactionValidated = {
      TransactionValidated(wallet.walletId, wallet.gandaruClientId, tranasctionId,  debit, credit, amount, LocalDateTime.now())
    }
  }

  final case class RollbackTransaction(
                                      transactionId: TransactionId,
                                      replyTo: ActorRef[Acknowledge[TransactionId]]
                                      ) extends Command {
    def asEvent(wallet: CreatedWallet, accountToDebit: Account, accountToCredit: Account, amount: Money): TransactionRolledback = {
      TransactionRolledback(wallet.walletId, wallet.gandaruClientId, transactionId, accountToDebit, accountToCredit, amount, LocalDateTime.now())
    }
  }

  final case class ListTransactions(accountId: AccountId, replyTo: ActorRef[Acknowledge[List[Transaction]]])
    extends Command
}

