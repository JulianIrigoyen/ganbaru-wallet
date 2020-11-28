package model.wallets.state

import akka.actor.typed.scaladsl.ActorContext
import sharding.EntityProvider
import model.settings.GandaruServiceSettings
import model.util.AcknowledgeWithResult
import model.wallets.{GandaruClientId, WalletCommands, WalletEvents, WalletId}
import model.wallets.state.WalletState.{EventsAnswerReplyEffect, WalletState}

case class EmptyWalletState(
                      walletId: WalletId,
                      settings: EntityProvider[GandaruServiceSettings.Command, GandaruClientId]
                      ) extends WalletState {

  override def applyCommand(command: WalletCommands.Command)(implicit context: ActorContext[WalletCommands.Command]): WalletState.EventsAnswerEffect = command match {
    case cmd@WalletCommands.CreateWalletWithNumber(gandaruClientId, walletNumber, confirmation, replyTo) =>
      println(s"Received create Wallet Command: $cmd")
      new EventsAnswerReplyEffect(this, List(cmd.asEvent(walletId)), replyTo, _ => AcknowledgeWithResult(walletId))

    case WalletCommands.AddAccount(cuit, balance, accountType, replyTo) => ???
    case WalletCommands.GetWallet(replyTo) => ???
  }



  override def applyEvent(event: WalletEvents.Event): WalletState = ???
}
