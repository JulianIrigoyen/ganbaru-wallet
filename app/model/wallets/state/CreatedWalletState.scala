package model.wallets.state

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.ActorContext
import model.settings.GandaruServiceSettings
import model.util.AcknowledgeWithResult
import model.wallets.WalletEvents.WalletCreated
import model.wallets.state.WalletState.{EventsAnswerReplyEffect, WalletState}
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

    }
  }

  override def applyEvent(event: WalletEvents.Event): WalletState = event match {
    case _: WalletCreated => this
  }
}
