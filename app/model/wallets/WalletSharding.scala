package model.wallets

import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{Entity, EntityTypeKey}
import model.settings.GandaruServiceSettings
import sharding.{EntityProvider, ShardingFactory}


class WalletSharding(settings: EntityProvider[GandaruServiceSettings.Command, GandaruClientId])
                      (implicit actorSystem: ActorSystem[_]) extends ShardingFactory[WalletCommands.Command, WalletId] {

  override protected val typedKey: EntityTypeKey[WalletCommands.Command] = EntityTypeKey[WalletCommands.Command]("Wallet")

  override protected def entity(): Entity[WalletCommands.Command, ShardingEnvelope[WalletCommands.Command]] =
    Entity(typedKey)(context => Wallet(WalletId(context.entityId), settings))

  override protected def entityId(id: WalletId): String = id.id
}