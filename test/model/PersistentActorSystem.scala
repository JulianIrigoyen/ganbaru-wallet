package model

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

object ActorSystemWithPersistence {

  private val testSystem = "WalletsSystemForTestSuite"

  def apply(): ActorSystem = {
    val actorSystem = ActorSystem(testSystem, ConfigFactory.load().getConfig(testSystem))
    actorSystem
  }


}


