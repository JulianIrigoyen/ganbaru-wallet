package model.settings

import java.util.UUID

import akka.Done
import akka.actor.typed.ActorRef
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import model.util.AcknowledgeWithResult
import model.wallets.GanbaruClientId
import org.nullvector.{EventAdapter, EventAdapterFactory}
import reactivemongo.api.bson.MacroConfiguration
/** This Actor exists to associate wallets to a specific service client */
object GanbaruServiceSettings {

  sealed trait Command {
    def replyTo: ActorRef[AcknowledgeWithResult[Done]]
  }

  sealed trait Event

  type State = Map[UUID, String]

  def apply(ganbaruClientId: GanbaruClientId): EventSourcedBehavior[GanbaruServiceSettings.Command, Event, State] = EventSourcedBehavior(
    persistenceId = persistenceId(ganbaruClientId),
    emptyState = Map.empty,
    commandHandler = commandHandler(ganbaruClientId),
    eventHandler = eventHandler()
  )

  def commandHandler(id: GanbaruClientId) :(State, Command) => Effect[Event, State] = (_, _) => Effect.none

  def eventHandler(): (State, Event) => State = (state, _) => state

  def persistenceId(ganbaruClientId: GanbaruClientId): PersistenceId = PersistenceId("Settings", ganbaruClientId.id)
}
