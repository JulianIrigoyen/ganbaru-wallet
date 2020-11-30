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

object GanbaruServiceSettings {

  sealed trait Command

  case class AddAttribute(attribute: String, ackTo: ActorRef[AcknowledgeWithResult[Done]]) extends Command

  sealed trait Event

  case class AttributeAddedOrUpdated(attribute: String) extends Event

  type State = Map[UUID, String]

  def apply(ganbaruClientId: GanbaruClientId): EventSourcedBehavior[GanbaruServiceSettings.Command, Event, State] = EventSourcedBehavior(
    persistenceId = persistenceId(ganbaruClientId),
    emptyState = Map.empty,
    commandHandler = commandHandler(ganbaruClientId),
    eventHandler = eventHandler()
  )

  def persistenceId(ganbaruClientId: GanbaruClientId): PersistenceId = PersistenceId("Settings", ganbaruClientId.id)

  def commandHandler(id: GanbaruClientId) :(State, Command) => Effect[Event, State] = (state, command) =>
    command match {
      case AddAttribute(attribute, ackTo) => Effect.persist[Event, State](AttributeAddedOrUpdated(attribute))
        .thenReply(ackTo)(_ => AcknowledgeWithResult(Done))
    }

  def eventHandler(): (State, Event) => State = (state, event) =>
    event match {
      case AttributeAddedOrUpdated(attribute) => state + (UUID.fromString(attribute) -> attribute)
    }

}
