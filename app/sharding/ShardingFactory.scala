package sharding

import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityRef, EntityTypeKey}

/** https://doc.akka.io/docs/akka/2.5/typed/cluster-sharding.html */

abstract class ShardingFactory[Command, Id](implicit system: ActorSystem[_]) {

  protected val typedKey: EntityTypeKey[Command]

  protected def entity(): Entity[Command, ShardingEnvelope[Command]]

  protected def entityId(id: Id): String

  private val clusterSharding = ClusterSharding(system)

  private val provider: EntityProvider[Command, Id] = {
    new EntityProvider[Command, Id] {
      override def entityFor(id: Id): EntityRef[Command] =
        clusterSharding.entityRefFor(typedKey, entityId(id))
    }
  }
  def entityProvider(): EntityProvider[Command, Id] = provider
  def initSharding(): Unit = clusterSharding.init(entity())
}

