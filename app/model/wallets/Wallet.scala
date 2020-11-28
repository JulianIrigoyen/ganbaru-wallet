package model.wallets

import java.time.LocalDateTime
import java.util.UUID

import akka.actor.typed.{Behavior, SupervisorStrategy}
import akka.actor.typed.scaladsl.Behaviors
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{EventSourcedBehavior, RetentionCriteria}
import model.settings.GandaruServiceSettings
import model.wallets.WalletCommands.Command
import model.wallets.WalletEvents.Event
import model.wallets.state.EmptyWalletState
import model.wallets.state.WalletState.{EventsAnswerEffect, WalletState}
import play.api.libs.json.{Format, Json}
import sharding.EntityProvider

/**
 *
 * Based on sample akka-persistence project generated at
 * https://developer.lightbend.com/start/?group=akka&project=akka-samples-persistence-scala
 *
 * This is an event sourced actor. It has a state, [[CreatedWallet]], which
 * stores the current information associated to the Wallet.
 *
 * Event sourced actors are interacted with by sending them commands,
 * see classes implementing [[WalletCommands.Command]].
 *
 * Commands get translated to events, see classes implementing [[WalletEvents.Event]].
 * It's the events that get persisted by the entity. Each event will have an event handler
 * registered for it, and an event handler updates the current state based on the event.
 * This will be done when the event is first created, and it will also be done when the entity is
 * loaded from the database - each event will be replayed to recreate the state
 * of the entity.
 */
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
        persistenceId = PersistenceId("Wallet", walletId.id),
        emptyState = state,
        commandHandler = applyCommand(_, _).applyEffect(),
        eventHandler = applyEvent
      )
    }

  }


}


object CreatedWallet {
  def confirmed(walletId: WalletId, gandaruClientId: GandaruClientId, walletNumber: WalletNumber, timeStamp: LocalDateTime): CreatedWallet = {
    CreatedWallet(
      walletId = walletId,
      gandaruClientId = gandaruClientId,
      walletNumber = walletNumber,
      timestamp = timeStamp
    )
  }
}
case class CreatedWallet(
                        walletId: WalletId,
                        gandaruClientId: GandaruClientId,
                        walletNumber: WalletNumber,
                        timestamp: LocalDateTime
                        )

final case class WalletId(id: String) extends AnyVal {
  override def toString: String =  id
}
object WalletId {
  def newWalletId(gandaruClientId: GandaruClientId): WalletId = new WalletId(s"${UUID.randomUUID()}-${gandaruClientId.id}")
}
final case class WalletNumber(number: Int) extends AnyVal

final case class GandaruClientId(id: String) extends AnyVal


