package sharding

import akka.cluster.sharding.typed.scaladsl.EntityRef

trait EntityProvider[Command, Id] {
  def entityFor(entityId: Id): EntityRef[Command]
}
