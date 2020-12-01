package adapter

import akka.actor.ActorSystem
import model.wallets.WalletEvents
import org.nullvector.{EventAdapter, ReactiveMongoEventSerializer}
import reactivemongo.api.bson.MacroConfiguration
import play.Module.WalletsSystem
import adapter.AdaptableEvents
import model.WalletFactoryEvents
import reactivemongo.api.bson.MacroConfiguration.Aux
import reactivemongo.api.bson.{BSONDocument, MacroConfiguration, MacroOptions, TypeNaming}

/** https://github.com/null-vector/akka-reactivemongo-plugin */

object EventAdapter  {

  implicit val conf: Aux[MacroOptions] = MacroConfiguration(discriminator = "_type", typeNaming = TypeNaming.SimpleName)

  def adaptEventsFor(system: ActorSystem) = {
    val serializer = ReactiveMongoEventSerializer(system)
    Seq(WalletEvents, WalletFactoryEvents).flatMap(_.adapt()) foreach { a =>  serializer.addEventAdapter(a) }
  }
}

trait AdaptableEvents {
  def adapt(): Seq[EventAdapter[_]]
}
