package persistence

import akka.actor.ActorSystem
import org.nullvector.ReactiveMongoEventSerializer
import reactivemongo.api.bson.MacroConfiguration.Aux
import reactivemongo.api.bson.{MacroConfiguration, MacroOptions, TypeNaming}

object EventAdapters {
  private val sumTypeDiscriminator = "_type"

  def register(actorSystem: ActorSystem): Unit = {
    val serializer = ReactiveMongoEventSerializer(actorSystem)
    implicit val conf: Aux[MacroOptions] = MacroConfiguration(discriminator = sumTypeDiscriminator, typeNaming = TypeNaming.SimpleName)
    Seq[EventAdaptersAware](
      //WalletFactory y WalletEvents
    )
      .flatMap(_.eventAdapters)
      .foreach(serializer.addEventAdapter)
  }
}


