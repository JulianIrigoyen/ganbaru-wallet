package model.wallets

import akka.Done
import akka.actor.ActorSystem
import akka.actor.testkit.typed.scaladsl.{ActorTestKit, TestProbe}
import akka.actor.typed.scaladsl.adapter._
import akka.persistence.typed.PersistenceId
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import play.api.libs.json.Json
import sharding.EntityProvider
import org.scalatest.FlatSpec
import model.ActorSystemWithPersistence
import akka.actor.typed.{ActorRef, ActorSystem}
import model.WalletFactory.ConfirmWallet
import model.settings.GanbaruServiceSettings
import model.util.Acknowledge
import model.wallets.Wallet.WalletConfirmation
import model.wallets.WalletCommands.CreateWalletWithNumber
import testing.tool.EntityProviderProbe

import scala.concurrent.duration.DurationInt


class WalletSpec extends FlatSpec with Matchers with BeforeAndAfterAll {

  private implicit val system = ActorSystemWithPersistence()
  private implicit val ec = system.dispatcher
  private implicit val typed = system.toTyped
  private val testKit: ActorTestKit = ActorTestKit(typed)

  private val settingsProvider: EntityProvider[GanbaruServiceSettings.Command, GanbaruClientId] =
    new EntityProviderProbe(clientId => testKit.spawn(GanbaruServiceSettings(clientId)))
  private val walletProvider: EntityProvider[WalletCommands.Command, WalletId] =
    new EntityProviderProbe(walletId => testKit.spawn(Wallet(walletId, settingsProvider)))

  override def afterAll(): Unit = {
    testKit.shutdownTestKit()
  }

  private def ignoredRef[T] = probeRef.ref

  private def probeRef[T] = testKit.createTestProbe[T]

  it should "recover from commands" in {
    val ganbaruClientId = GanbaruClientId("777")
    val walletId = WalletId("2222")
    val wallet = testKit.spawn(
      Wallet.recoverFrom(walletId, settingsProvider,
        List(
          CreateWalletWithNumber(ganbaruClientId, WalletNumber(22), WalletConfirmation("2223334445"), ignoredRef[Acknowledge[WalletId]])
        )
      )
    )
    println(getWallet(wallet))
  }

  it should "add accounts" in {}
  it should "get an account" in {}
  it should "get the bulkiest account in a wallet" in {}
  it should "deposit to an account in a wallet" in {}
  it should "withdraw from an account in a wallet" in {}
  it should "transfer from an account to another " in {}
  it should "rollback a transaction" in {}
  it should "list transactions" in {}

  def getWallet(wallet: ActorRef[WalletCommands.Command]): CreatedWallet = {
    val getWallet = probeRef[Acknowledge[CreatedWallet]]
    wallet ! WalletCommands.GetWallet(getWallet.ref)
    getWallet.expectMessageType[Acknowledge[CreatedWallet]](5.second).get
  }



}