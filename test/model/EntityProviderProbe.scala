package testing.tool

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.cluster.sharding.typed.scaladsl.EntityRef
import akka.util.Timeout
import model.MockitoSweet
import org.mockito.invocation.InvocationOnMock
import sharding.EntityProvider

class EntityProviderProbe[Command, Id](
                                        factory: Id => ActorRef[Command]
                                      )(implicit typed: ActorSystem[_]) extends EntityProvider[Command, Id] with MockitoSweet {

  import akka.actor.typed.scaladsl.AskPattern._

  override def entityFor(id: Id): EntityRef[Command] = {
    val ref = factory(id)
    val entityRef = mock[EntityRef[Command]]
    entityRef.!(any).thenAnswer((invo: InvocationOnMock) => ref ! invo.getArgument[Command](0))
    entityRef.ask(any)(any).thenAnswer { (invo: InvocationOnMock) =>
      implicit val timeout = invo.getArgument[Timeout](1)
      ref.ask(invo.getArgument[ActorRef[_] => Command](0))(timeout, typed.scheduler)
    }
    entityRef
  }

}
