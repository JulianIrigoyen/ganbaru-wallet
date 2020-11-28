package model.wallets.state

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.ActorContext
import model.{Account, AccountId}
import model.settings.GandaruServiceSettings
import model.util.{AcknowledgeWithFailure, AcknowledgeWithResult}
import model.wallets.WalletEvents.WalletCreated
import model.wallets.state.WalletState.{EventsAnswerReplyEffect, NonEventsAnswerReplyEffect, WalletState}
import model.wallets.{CreatedWallet, GandaruClientId, WalletCommands, WalletEvents, WalletId}
import sharding.EntityProvider

case class CreatedWalletState(
                               wallet: CreatedWallet,
                               settings: EntityProvider[GandaruServiceSettings.Command, GandaruClientId]
                        ) extends WalletState {

  override def applyCommand(command: WalletCommands.Command)(implicit context: ActorContext[WalletCommands.Command]): WalletState.EventsAnswerEffect = {
    implicit val as: ActorSystem[Nothing] = context.system

    command match {
      case WalletCommands.GetWallet(replyTo) =>
        println(s"Received get Wallet: $wallet")
        new EventsAnswerReplyEffect(this, Nil, replyTo, _ => AcknowledgeWithResult(wallet))

      case addAcc @ WalletCommands.AddAccount(cuit, accountType, currency, replyTo) =>
        println(s"Creating wallet account")
        wallet.accounts.collectFirst {
          case acc: Account if acc.accountType == accountType && acc.balance.currency == currency => acc
        } match {
          case Some(_) => new NonEventsAnswerReplyEffect[AcknowledgeWithFailure[AccountId]](replyTo,
            AcknowledgeWithFailure(s"User with cuit $cuit already has a $accountType account for $currency . "))

          case None =>
            val newAccountId = AccountId.newAccountId
            val event = List(addAcc.asEvent(wallet, newAccountId))
            new EventsAnswerReplyEffect[AcknowledgeWithResult[AccountId]](this, event, replyTo, _ => AcknowledgeWithResult(newAccountId))
        }

    }
  }

  override def applyEvent(event: WalletEvents.Event): WalletState = event match {
    case _: WalletCreated => this
    case WalletEvents.AccountAdded(walletId, gandaruClientId, accountId, cuit, accountType, balance, dateOpened) =>
      val newAccount = Account(walletId, gandaruClientId, accountId, cuit, accountType, balance, dateOpened)
      copy(wallet.copy(accounts = wallet.accounts :+ newAccount))
  }
}
