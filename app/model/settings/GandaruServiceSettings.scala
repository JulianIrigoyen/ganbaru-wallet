package model.settings

import java.util.UUID

import akka.Done
import akka.actor.typed.ActorRef
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.EventSourcedBehavior
import model.GandaruClientId
import org.nullvector.EventAdapter
import persistence.EventAdaptersAware
import reactivemongo.api.bson.MacroConfiguration

object GandaruServiceSettings extends EventAdaptersAware {

  sealed trait Command

  case class AddAttribute(attribute: String, ackTo: ActorRef[Done]) extends Command

  sealed trait Event

  case class AttributeAddedOrUpdated(attribute: String) extends Event

  type State = Map[UUID, String]

  def apply(gandaruClientId: GandaruClientId): EventSourcedBehavior[GandaruServiceSettings.Command, Event, State] = EventSourcedBehavior(
    persistenceId = persistenceId(gandaruClientId.id),
    emptyState = Map.empty,
    commandHandler = commandHandler(gandaruClientId),
    eventHandler = eventHandler()
  )

  def persistenceId(gandaruClientId: Int): PersistenceId = PersistenceId("Settings", gandaruClientId.toString)

  def commandHandler(id: GandaruClientId) = ???
  def eventHandler() = ???

  override def eventAdapters(implicit macroConfiguration: MacroConfiguration): Seq[EventAdapter[_]] = ???


}
