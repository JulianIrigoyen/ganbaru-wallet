package play

import akka.actor.{ActorSystem, Props, typed}
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import akka.persistence.journal.leveldb.SharedLeveldbStore
import com.google.inject.AbstractModule
import com.typesafe.config.{Config, ConfigValueFactory}
import sharding.EntityProvider
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.concurrent.AkkaGuiceSupport
import play.api.{Configuration, Environment, Mode}
import model.settings.GanbaruServiceSettingsSharding
import model.{WalletFactory, WalletFactorySharding}
import model.wallets.{GanbaruClientId, WalletCommands, WalletId, WalletSharding}
import org.nullvector.ReactiveMongoEventSerializer
import adapter.EventAdapter
import reactivemongo.api.bson.MacroConfiguration.Aux
import reactivemongo.api.bson.{MacroConfiguration, MacroOptions, TypeNaming}

/** https://www.programcreek.com/scala/play.api.libs.concurrent.AkkaGuiceSupport */

object Module {

  class WalletsSystem(actorSystem: akka.actor.typed.ActorSystem[_])

}

class Module(environment: Environment, configuration: Configuration) extends AbstractModule with ScalaModule with AkkaGuiceSupport {

  import akka.actor.typed.scaladsl.adapter._
  import play.Module.WalletsSystem
  import model.wallets.WalletEvents._

  implicit val conf: Aux[MacroOptions] = MacroConfiguration(discriminator = "_type", typeNaming = TypeNaming.SimpleName)

  private val actorSystemName = "WalletsSystem"
  private val config: Config = configuration.underlying
  private val walletsSystem: ActorSystem = ActorSystem(actorSystemName, config.getConfig(actorSystemName))

  private val serializer = ReactiveMongoEventSerializer(walletsSystem)


  override def configure(): Unit = {

    /** A shared LevelDB instance is started by instantiating the SharedLeveldbStore actor. */
    //walletsSystem.actorOf(Props[SharedLeveldbStore](), "store")

    val typedWalletsSystem: typed.ActorSystem[Nothing] = walletsSystem.toTyped


    /** Akka Management does not start automatically and the routes have to be exposed manually */
    AkkaManagement(walletsSystem).start()
    ClusterBootstrap(walletsSystem).start()

    val settingsSharding = new GanbaruServiceSettingsSharding()(typedWalletsSystem)
    val walletSharding = new WalletSharding(settingsSharding.entityProvider())(typedWalletsSystem)
    val walletFactory = new WalletFactorySharding(walletSharding.entityProvider())(typedWalletsSystem)

    settingsSharding.initSharding()
    walletFactory.initSharding()
    walletSharding.initSharding()

    /** Bindings for Guice DI */
    bind[WalletsSystem].toInstance(new WalletsSystem(typedWalletsSystem))
    bind[EntityProvider[WalletCommands.Command, WalletId]].toInstance(walletSharding.entityProvider())
    bind[EntityProvider[WalletFactory.Command, GanbaruClientId]].toInstance(walletFactory.entityProvider())
  }

  /** Adapt Events */
  EventAdapter.adaptEventsFor(walletsSystem)

}