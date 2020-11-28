package model.wallets

import java.time.LocalDateTime

import akka.Done
import akka.actor.typed.ActorRef
import model.util.Acknowledge
import model.wallets.Wallet.WalletConfirmation
import model.wallets.WalletEvents.WalletCreated
import model.{AccountType, Money}


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


  final case class AddAccount(cuit: String, balance: Option[Money], accountType: AccountType.AccountType, replyTo: ActorRef[Done]) extends Command
  final case class GetWallet(replyTo: ActorRef[Done]) extends Command

}

