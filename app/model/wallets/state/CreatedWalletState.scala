package model.wallets.state

import akka.actor.typed.scaladsl.ActorContext
import model.settings.GandaruServiceSettings
import model.wallets.state.WalletState.WalletState
import model.wallets.{CreatedWallet, GandaruClientId, WalletCommands, WalletEvents, WalletId}
import sharding.EntityProvider

case class CreatedWalletState(
                               wallet: CreatedWallet,
                               settings: EntityProvider[GandaruServiceSettings.Command, GandaruClientId]
                        ) extends WalletState {

  override def applyCommand(command: WalletCommands.Command)(implicit contect: ActorContext[WalletCommands.Command]): WalletState.EventsAnswerEffect = command match {
    case WalletCommands.CreateWalletWithNumber(gandaruClientId, walletNumber, confirmation, replyTo) =>  ???
    case WalletCommands.AddAccount(cuit, balance, accountType, replyTo) => ???
    case WalletCommands.GetWallet(replyTo) => ???
  }

  override def applyEvent(event: WalletEvents.Event): WalletState = ???
}
