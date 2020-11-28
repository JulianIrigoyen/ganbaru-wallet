package model.wallets

import java.util.UUID

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.EventSourcedBehavior
import model.settings.GandaruServiceSettings
import model.wallets.WalletCommands.Command
import model.wallets.WalletEvents.Event
import model.wallets.state.EmptyWalletState
import model.wallets.state.WalletState.{EventsAnswerEffect, WalletState}
import play.api.libs.json.{Format, Json}
import sharding.EntityProvider


object Wallet {

  case class WalletConfirmation(cuit: String)

  def apply(walletId: WalletId, settings: EntityProvider[GandaruServiceSettings.Command, GandaruClientId]): Behavior[Command] = {
    recoverFrom(walletId, settings, Nil)
  }

  def recoverFrom(walletId: WalletId, settings: EntityProvider[GandaruServiceSettings.Command, GandaruClientId], commands: List[Command]): Behavior[WalletCommands.Command] = {
    Behaviors.setup { implicit context =>

      val applyEvent: (WalletState, Event) => WalletState = _ applyEvent _
      val applyCommand: (WalletState, Command) => EventsAnswerEffect = _.applyCommand(_)

      val state = commands.foldLeft[WalletState](EmptyWalletState(walletId, settings))(
        (state, command) => applyCommand(state, command).applyEventsFrom(state, applyEvent)
      )

      EventSourcedBehavior[WalletCommands.Command, Event, WalletState](
        persistenceId = PersistenceId("Wallet", walletId.walletId),
        emptyState = state,
        commandHandler = applyCommand(_, _).applyEffect(),
        eventHandler = applyEvent
      )
    }

  }


}

case class WalletId(walletId: String)

object WalletId {
  implicit val walletId: Format[WalletId] = Json.format
  def newWalletId: WalletId = new WalletId(UUID.randomUUID().toString)
}
final case class WalletNumber(number: Int)

final case class GandaruClientId(id: String) extends AnyVal

