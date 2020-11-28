package model.wallets.state

import akka.actor.typed.scaladsl.ActorContext
import model.{GandaruClientId, WalletId}
import sharding.EntityProvider
import model.settings.GandaruServiceSettings
import model.wallets.{WalletCommands, WalletEvents}
import model.wallets.state.WalletState.WalletState

case class EmptyWalletState(
                      walletId: WalletId,
                      settings: EntityProvider[GandaruServiceSettings.Command, GandaruClientId]
                      ) extends WalletState {

  override def applyCommand(command: WalletCommands.Command)(implicit contect: ActorContext[WalletCommands.Command]): WalletState.EventsAnswerEffect = ???

  override def applyEvent(event: WalletEvents.Event): WalletState = ???
}
