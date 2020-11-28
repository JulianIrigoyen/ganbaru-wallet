package play

import akka.actor.{ActorSystem, Props, typed}
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import com.google.inject.AbstractModule
import com.typesafe.config.{Config, ConfigValueFactory}
import persistence.EventAdapters
import sharding.EntityProvider
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.concurrent.AkkaGuiceSupport
import play.api.{Configuration, Environment, Mode}
import model.settings.GandaruServiceSettingsSharding
import model.{WalletFactory, WalletFactorySharding}
import model.wallets.{GandaruClientId, WalletCommands, WalletId, WalletSharding}

/** https://www.programcreek.com/scala/play.api.libs.concurrent.AkkaGuiceSupport */

object Module {

  class WalletsSystem(actorSystem: akka.actor.typed.ActorSystem[_])

}

class Module(environment: Environment, configuration: Configuration) extends AbstractModule with ScalaModule with AkkaGuiceSupport {

  import akka.actor.typed.scaladsl.adapter._
  import play.Module.WalletsSystem

  private val actorSystemName = "WalletsSystem"
  private val config: Config = configuration.underlying
  private val walletsSystem: ActorSystem = ActorSystem(actorSystemName, config.getConfig(actorSystemName))

  override def configure(): Unit = {
    val typedWalletsSystem: typed.ActorSystem[Nothing] = walletsSystem.toTyped

    EventAdapters.register(walletsSystem)

    /** Akka Management does not start automatically and the routes have to be exposed manually */
    AkkaManagement(walletsSystem).start()
    ClusterBootstrap(walletsSystem).start()

    val settingsSharding = new GandaruServiceSettingsSharding()(typedWalletsSystem)
    val walletSharding = new WalletSharding(settingsSharding.entityProvider())(typedWalletsSystem)
    val walletFactory = new WalletFactorySharding(walletSharding.entityProvider())(typedWalletsSystem)

    settingsSharding.initSharding()
    walletFactory.initSharding()
    walletSharding.initSharding()

    bind[WalletsSystem].toInstance(new WalletsSystem(typedWalletsSystem))
    bind[EntityProvider[WalletCommands.Command, WalletId]].toInstance(walletSharding.entityProvider())
    bind[EntityProvider[WalletFactory.Command, GandaruClientId]].toInstance(walletFactory.entityProvider())

  }

}