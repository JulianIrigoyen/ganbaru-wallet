package model.wallets

import akka.Done
import akka.actor.typed.ActorRef
import model.WalletAggregate.WalletConfirmation
import model.{AccountType, ClientId, Money, WalletId}


object WalletCommands {

  sealed trait Command

  final case class CreateWallet(clientId: ClientId, walletConfirmation: WalletConfirmation, replyTo: ActorRef[Done]) extends Command
  final case class AddAccount(cuit: String, balance: Option[Money], accountType: AccountType.AccountType, replyTo: ActorRef[Done]) extends Command
  final case class GetWallet(replyTo: ActorRef[Done]) extends Command

}

