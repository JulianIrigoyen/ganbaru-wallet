package model.wallets

import java.time.LocalDateTime

import akka.Done
import akka.actor.typed.ActorRef
import model.Money.Currency
import model.util.Acknowledge
import model.wallets.Wallet.WalletConfirmation
import model.wallets.WalletEvents.{AccountAdded, WalletCreated}
import model.{AccountId, AccountType, Money}

/** This interface defines all the commands that the Wallet persistent actor supports. */
object WalletCommands {

  sealed trait Command

  final case class CreateWalletWithNumber(
                                 gandaruClientId: GandaruClientId,
                                 walletNumber: WalletNumber,
                                 confirmation: WalletConfirmation,
                                 replyTo: ActorRef[Acknowledge[WalletId]]) extends Command {
    def asEvent(walletId: WalletId): WalletCreated = {
      WalletCreated(walletId, gandaruClientId, walletNumber, confirmation, LocalDateTime.now())
    }
  }


  final case class GetWallet(replyTo: ActorRef[Acknowledge[CreatedWallet]]) extends Command


  final case class AddAccount(
                               cuit: String,
                               accountType: AccountType.AccountType,
                               currency: Currency,
                               replyTo: ActorRef[Acknowledge[AccountId]]) extends Command {
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

}

