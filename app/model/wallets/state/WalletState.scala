package model.wallets.state

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.ActorContext
import akka.persistence.typed.scaladsl.Effect
import model.wallets.{WalletCommands, WalletEvents}
import model.wallets.WalletEvents.Event

object WalletState {

  type Reply = WalletState => Unit

  type ReplyEffect = akka.persistence.typed.scaladsl.ReplyEffect[Event, WalletState]

  sealed trait EventsAnswerEffect {
    def applyEffect(): ReplyEffect
    def applyEventsFrom(state: WalletState, f: (WalletState, Event) => WalletState): WalletState
  }

  final class EventsAnswerReplyEffect[T](fromState: WalletState,
                                         events: List[WalletEvents.Event],
                                         replyTo: ActorRef[T],
                                         answer: WalletState => T) extends EventsAnswerEffect {

    override def applyEffect(): ReplyEffect = {
      events match {
        case ::(_, _) => Effect.persist(events).thenReply(replyTo)(answer)
        case Nil => Effect.reply(replyTo)(answer(fromState))
      }
    }

    override def applyEventsFrom(state: WalletState, f: (WalletState, Event) => WalletState): WalletState = {
      events.foldLeft(state)(f)

    }
  }

  final class NonEventsAnswerReplyEffect[T](replyTo: ActorRef[T], answer: T) extends EventsAnswerEffect {
    override def applyEffect(): ReplyEffect = {
      Effect.reply(replyTo)(answer)
    }

    override def applyEventsFrom(state: WalletState, f: (WalletState, Event) => WalletState): WalletState = state
  }

  trait WalletState {
    def applyCommand(command: WalletCommands.Command)(implicit contect: ActorContext[WalletCommands.Command]): EventsAnswerEffect
    def applyEvent(event: Event): WalletState
  }

}
