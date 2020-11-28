package model.settings

import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{Entity, EntityTypeKey}
import model._
import model.wallets.GandaruClientId
import sharding.ShardingFactory

class GandaruServiceSettingsSharding(implicit system: ActorSystem[_]) extends ShardingFactory[GandaruServiceSettings.Command, GandaruClientId] {
  override protected val typedKey: EntityTypeKey[GandaruServiceSettings.Command] = EntityTypeKey[GandaruServiceSettings.Command]("GandaruServiceSettings")

  override protected def entity(): Entity[GandaruServiceSettings.Command, ShardingEnvelope[GandaruServiceSettings.Command]] =
    Entity(typedKey)(context => GandaruServiceSettings(GandaruClientId(context.entityId)))

  override protected def entityId(id: GandaruClientId): String = id.id
}