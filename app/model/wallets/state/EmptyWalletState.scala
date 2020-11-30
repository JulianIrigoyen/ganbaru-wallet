package model.wallets.state

import akka.actor.typed.scaladsl.ActorContext
import sharding.EntityProvider
import model.settings.GanbaruServiceSettings
import model.util.AcknowledgeWithResult
import model.wallets.{CreatedWallet, GanbaruClientId, WalletCommands, WalletEvents, WalletId}
import model.wallets.state.WalletState.{EventsAnswerReplyEffect, NonEventsAnswerReplyEffect, WalletState}

case class EmptyWalletState(
                      walletId: WalletId,
                      settings: EntityProvider[GanbaruServiceSettings.Command, GanbaruClientId]
                      ) extends WalletState {

  override def applyCommand(command: WalletCommands.Command)(implicit context: ActorContext[WalletCommands.Command]): WalletState.EventsAnswerEffect = command match {
    case cmd@WalletCommands.CreateWalletWithNumber(_, _, _, replyTo) =>
      println(s"Received create Wallet Command: $cmd")
      new EventsAnswerReplyEffect(this, List(cmd.asEvent(walletId)), replyTo, _ => AcknowledgeWithResult(walletId))
  }



  override def applyEvent(event: WalletEvents.Event): WalletState = event match {
    case WalletEvents.WalletCreated(walletId, ganbaruClientId, walletNumber, confirmation, timestamp) =>
      val wallet: CreatedWallet = CreatedWallet.confirmed(walletId, ganbaruClientId, walletNumber, timestamp)
      println(s"Created Wallet: $wallet")
      CreatedWalletState(wallet, settings)

    case _ => throw new IllegalStateException(s"Can not apply $event on an Empty Wallet")
  }
}
