package model

import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{Entity, EntityTypeKey}
import model.wallets.{GanbaruClientId, WalletCommands, WalletId}
import sharding.{EntityProvider, ShardingFactory}


class WalletFactorySharding(walletProvider: EntityProvider[WalletCommands.Command, WalletId])
                             (implicit actorSystem: ActorSystem[_]) extends ShardingFactory[WalletFactory.Command, GanbaruClientId] {

  override protected val typedKey: EntityTypeKey[WalletFactory.Command] = EntityTypeKey[WalletFactory.Command]("WalletFactory")

  override protected def entity(): Entity[WalletFactory.Command, ShardingEnvelope[WalletFactory.Command]] =
    Entity(typedKey)(context => WalletFactory(GanbaruClientId(context.entityId), walletProvider))

  override protected def entityId(id: GanbaruClientId): String = id.id
}
