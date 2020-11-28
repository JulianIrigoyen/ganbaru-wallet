package model.settings

import java.util.UUID

import akka.Done
import akka.actor.typed.ActorRef
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import model.util.AcknowledgeWithResult
import model.wallets.GandaruClientId
import org.nullvector.{EventAdapter, EventAdapterFactory}
import persistence.EventAdaptersAware
import reactivemongo.api.bson.MacroConfiguration

object GandaruServiceSettings extends EventAdaptersAware {

  sealed trait Command

  case class AddAttribute(attribute: String, ackTo: ActorRef[AcknowledgeWithResult[Done]]) extends Command

  sealed trait Event

  case class AttributeAddedOrUpdated(attribute: String) extends Event

  type State = Map[UUID, String]

  def apply(gandaruClientId: GandaruClientId): EventSourcedBehavior[GandaruServiceSettings.Command, Event, State] = EventSourcedBehavior(
    persistenceId = persistenceId(gandaruClientId),
    emptyState = Map.empty,
    commandHandler = commandHandler(gandaruClientId),
    eventHandler = eventHandler()
  )

  def persistenceId(gandaruClientId: GandaruClientId): PersistenceId = PersistenceId("Settings", gandaruClientId.id)

  def commandHandler(id: GandaruClientId) :(State, Command) => Effect[Event, State] = (state, command) =>
    command match {
      case AddAttribute(attribute, ackTo) => Effect.persist[Event, State](AttributeAddedOrUpdated(attribute))
        .thenReply(ackTo)(_ => AcknowledgeWithResult(Done))
    }

  def eventHandler(): (State, Event) => State = (state, event) =>
    event match {
      case AttributeAddedOrUpdated(attribute) => state + (UUID.fromString(attribute) -> attribute)
    }

  override def eventAdapters(implicit macroConfiguration: MacroConfiguration): Seq[EventAdapter[_]] = Seq(
    EventAdapterFactory.adapt[AttributeAddedOrUpdated]("AttributeAddedOrUpdated")
  )


}
