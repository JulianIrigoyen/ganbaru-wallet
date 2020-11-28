package model

import akka.Done
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, RetentionCriteria}
import model.util.Acknowledge
import model.wallets.Wallet.WalletConfirmation
import model.wallets.{GandaruClientId, WalletCommands, WalletId, WalletNumber}
import model.wallets.WalletCommands.CreateWalletWithNumber
import sharding.EntityProvider

object WalletFactory {

  sealed trait Command

  case class ConfirmWallet(
                            confirmation: WalletConfirmation,
                            replyTo: ActorRef[Acknowledge[WalletId]]
                            ) extends Command

  sealed trait Event
  case object WalletCreated extends Event

  case class WalletNumberState(number: Int)

  def apply(gandaruClientId: GandaruClientId, walletProvider: EntityProvider[WalletCommands.Command, WalletId]): Behavior[WalletFactory.Command] = {
    Behaviors.setup { implicit context =>
      EventSourcedBehavior[WalletFactory.Command, Event, WalletNumberState](
        persistenceId = PersistenceId("WalletNumber", gandaruClientId.id),
        emptyState = WalletNumberState(222),
        commandHandler = commandHandler(gandaruClientId, walletProvider),
        eventHandler = eventHandler
      )
        .withRetention(RetentionCriteria.snapshotEvery(10, 1).withDeleteEventsOnSnapshot)
    }
  }

  private def commandHandler(
                              gandaruClientId: GandaruClientId, walletProvider: EntityProvider[WalletCommands.Command, WalletId]
                            )
                            (implicit context: ActorContext[Command]): (WalletNumberState, Command) => Effect[Event, WalletNumberState] = {
    (_, command) =>
      command match {
        case ConfirmWallet(confirmation, replyTo) =>
          Effect.persist(WalletCreated)
            .thenRun { state =>
              val walletNumber = WalletNumber(state.number)
              val walletId = WalletId.newWalletId
              walletProvider.entityFor(walletId) ! CreateWalletWithNumber(gandaruClientId, walletNumber, confirmation, replyTo)
            }

      }
  }

  private def eventHandler: (WalletNumberState, Event) => WalletNumberState = { (state, event) =>
    event match {
      case WalletCreated => state.copy(number = state.number + 1)
    }
  }




}
