package model

import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{Entity, EntityTypeKey}
import model.wallets.WalletCommands
import sharding.{EntityProvider, ShardingFactory}


class WalletFactorySharding(walletProvider: EntityProvider[WalletCommands.Command, WalletId])
                             (implicit actorSystem: ActorSystem[_]) extends ShardingFactory[WalletFactory.Command, GandaruClientId] {

  override protected val typedKey: EntityTypeKey[WalletFactory.Command] = EntityTypeKey[WalletFactory.Command]("WalletFactory")

  override protected def entity(): Entity[WalletFactory.Command, ShardingEnvelope[WalletFactory.Command]] =
    Entity(typedKey)(context => WalletFactory(ClientId(context.entityId), walletProvider))

  override protected def entityId(id: GandaruClientId): String = id.id.toString
}
