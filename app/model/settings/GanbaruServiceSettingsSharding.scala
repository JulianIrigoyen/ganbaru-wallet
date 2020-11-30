package model.settings

import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{Entity, EntityTypeKey}
import model._
import model.wallets.GanbaruClientId
import sharding.ShardingFactory

class GanbaruServiceSettingsSharding(implicit system: ActorSystem[_]) extends ShardingFactory[GanbaruServiceSettings.Command, GanbaruClientId] {
  override protected val typedKey: EntityTypeKey[GanbaruServiceSettings.Command] = EntityTypeKey[GanbaruServiceSettings.Command]("GanbaruServiceSettings")

  override protected def entity(): Entity[GanbaruServiceSettings.Command, ShardingEnvelope[GanbaruServiceSettings.Command]] =
    Entity(typedKey)(context => GanbaruServiceSettings(GanbaruClientId(context.entityId)))

  override protected def entityId(id: GanbaruClientId): String = id.id
}