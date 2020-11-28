package model

import akka.Done
import akka.actor.typed.RecipientRef.RecipientRefOps
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, RetentionCriteria}
import model.WalletAggregate.WalletConfirmation
import model.wallets.WalletCommands
import model.wallets.WalletCommands.CreateWallet
import sharding.EntityProvider

object WalletFactory {

  sealed trait Command

  case class ConfirmWallet(
                            clientId: ClientId,
                            confirmation: WalletConfirmation,
                            replyTo: ActorRef[Done]
                            ) extends Command

  sealed trait Event
  case object WalletCreated extends Event

  case class WalletNumberState(number: Int)

  def apply(clientId: ClientId, walletProvider: EntityProvider[WalletCommands.Command, WalletId]): Behavior[WalletFactory.Command] = {
    Behaviors.setup { implicit context =>
      EventSourcedBehavior[WalletFactory.Command, Event, WalletNumberState](
        persistenceId = PersistenceId("WalletNumber", clientId.id),
        emptyState = WalletNumberState(222),
        commandHandler = commandHandler(clientId, walletProvider),
        eventHandler = eventHandler
      )
        .withRetention(RetentionCriteria.snapshotEvery(10, 1).withDeleteEventsOnSnapshot)
    }
  }

  private def commandHandler(
                              clientId: ClientId, walletProvider: EntityProvider[WalletCommands.Command, WalletId]
                            )
                            (implicit context: ActorContext[Command]): (WalletNumberState, Command) => Effect[Event, WalletNumberState] = {
    (_, command) =>
      command match {
        case ConfirmWallet(walletId, confirmation, replyTo)=>
          Effect.persist(WalletCreated)
            .thenRun { state =>
              val walletNumber = WalletNumber(state.number)
              val walletId = WalletId.newWalletId
              walletProvider.entityFor(walletId) ! CreateWallet(clientId, confirmation, replyTo)
            }

      }
  }

  private def eventHandler: (WalletNumberState, Event) => WalletNumberState = { (state, event) =>
    event match {
      case WalletCreated => state.copy(number = state.number + 1)
    }
  }




}
