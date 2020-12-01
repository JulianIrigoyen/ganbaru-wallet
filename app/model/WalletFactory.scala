package model

import adapter.AdaptableEvents
import akka.Done
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, RetentionCriteria}
import model.util.Acknowledge
import model.wallets.Wallet.WalletConfirmation
import model.wallets.{GanbaruClientId, WalletCommands, WalletId, WalletNumber}
import model.wallets.WalletCommands.CreateWalletWithNumber
import org.nullvector.{EventAdapter, EventAdapterFactory}
import sharding.EntityProvider

object WalletFactory {

  sealed trait Command

  case class ConfirmWallet(
                            confirmation: WalletConfirmation,
                            replyTo: ActorRef[Acknowledge[WalletId]]
                            ) extends Command


  case class WalletNumberState(number: Int)

  def apply(ganbaruClientId: GanbaruClientId, walletProvider: EntityProvider[WalletCommands.Command, WalletId]): Behavior[WalletFactory.Command] = {
    Behaviors.setup { implicit context =>
      EventSourcedBehavior[WalletFactory.Command, WalletFactoryEvents.Event, WalletNumberState](
        persistenceId = PersistenceId("WalletNumber", ganbaruClientId.id),
        emptyState = WalletNumberState(222),
        commandHandler = commandHandler(ganbaruClientId, walletProvider),
        eventHandler = eventHandler
      )
    }
  }

  private def commandHandler(
                              ganbaruClientId: GanbaruClientId, walletProvider: EntityProvider[WalletCommands.Command, WalletId]
                            )
                            (implicit context: ActorContext[Command]): (WalletNumberState, Command) => Effect[WalletFactoryEvents.Event, WalletNumberState] = {
    (_, command) =>
      command match {
        case ConfirmWallet(confirmation, replyTo) =>
          Effect.persist(WalletFactoryEvents.WalletCreated()).thenRun { state: WalletNumberState =>
              val walletNumber = WalletNumber(state.number)
              val walletId = WalletId.newWalletId(ganbaruClientId)
              walletProvider.entityFor(walletId) ! CreateWalletWithNumber(ganbaruClientId, walletNumber, confirmation, replyTo)
            }
      }
  }

  private def eventHandler: (WalletNumberState, WalletFactoryEvents.Event) => WalletNumberState = { (state, event) =>
    event match {
      case WalletFactoryEvents.WalletCreated() => state.copy(number = state.number + 1)
    }
  }

}

object WalletFactoryEvents extends AdaptableEvents {
  sealed trait Event

  case class WalletCreated() extends Event

  override def adapt(): Seq[EventAdapter[_]] = Seq(
    EventAdapterFactory.adapt[WalletFactoryEvents.WalletCreated]( "WalletCreated"),

  )
}
