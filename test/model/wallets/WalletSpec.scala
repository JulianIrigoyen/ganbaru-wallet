package model.wallets

import java.util.UUID

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
import model.{Account, AccountId, AccountType, ActorSystemWithPersistence, Money, TransactionId}
import akka.actor.typed.{ActorRef, ActorSystem}
import model.Money._
import model.WalletFactory.ConfirmWallet
import model.settings.GanbaruServiceSettings
import model.util.{Acknowledge, AcknowledgeWithFailure, AcknowledgeWithResult}
import model.wallets.Wallet.WalletConfirmation
import model.wallets.WalletCommands.{AddAccount, AttemptTransaction, CreateWalletWithNumber, Deposit, GetAccount, GetBulkiestAccount, Withdraw}
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

  private def randomId = UUID.randomUUID().toString

  it should "recover from commands" in getWallet(createWalletAndShootCommands(WalletId(randomId), List.empty))

  it should "add accounts" in {
    val wallet = createWalletAndShootCommands(WalletId(randomId), List.empty)
    addAccountToWallet(wallet, randomId, AccountType.P2P)
    getWallet(wallet).accounts.size shouldBe 1
  }

  it should "get an account" in {
    val probe = probeRef[Acknowledge[Account]]
    val wallet = createWalletAndShootCommands(WalletId(randomId), List.empty)
    val accountId = addAccountToWallet(wallet, randomId,AccountType.P2P)

    wallet ! GetAccount(accountId, probe.ref)
    probe.expectMessageType[AcknowledgeWithResult[Account]]
      .get.accountType shouldBe AccountType.P2P
  }

  it should "deposit to an account in a wallet" in {
    val probe = probeRef[Acknowledge[Account]]
    val wallet = createWalletAndShootCommands(WalletId(randomId), List.empty)
    val accountId = addAccountToWallet(wallet, randomId, AccountType.P2P)
    wallet ! Deposit(accountId, Money(10000, ARS), probe.ref)
    probe.expectMessageType[AcknowledgeWithResult[Account]]

    getWallet(wallet).accounts.head.balance.amount shouldBe 10000
  }
  it should "properly withdraw from an account in a wallet" in {
    val probe = probeRef[Acknowledge[Account]]
    val wallet = createWalletAndShootCommands(WalletId(randomId), List.empty)
    val accountId = addAccountToWallet(wallet, randomId, AccountType.P2P)

    wallet ! Deposit(accountId, Money(10000, ARS), probe.ref)
    probe.expectMessageType[AcknowledgeWithResult[Account]]

    wallet ! Withdraw(accountId, Money(90000, ARS), probe.ref)
    probe.expectMessageType[AcknowledgeWithFailure[Account]]

    wallet ! Withdraw(accountId, Money(9000, ARS), probe.ref)
    probe.expectMessageType[AcknowledgeWithResult[Account]]
      .get.balance.amount shouldBe 1000

  }

  it should "properly get the bulkiest account in a wallet" in {
    val probe = probeRef[Acknowledge[Account]]
    val wallet = createWalletAndShootCommands(WalletId(randomId), List.empty)
    val p2pId = addAccountToWallet(wallet, randomId, AccountType.P2P)
    val spotId = addAccountToWallet(wallet, randomId, AccountType.Spot)

    wallet ! Deposit(p2pId, Money(20000, ARS), probe.ref)
    probe.expectMessageType[AcknowledgeWithResult[Account]]

    wallet ! Deposit(spotId, Money(22222, ARS), probe.ref)
    probe.expectMessageType[AcknowledgeWithResult[Account]]

    wallet ! GetBulkiestAccount(probe.ref)
    probe.expectMessageType[AcknowledgeWithResult[Account]]
      .get.accountId == spotId
  }

  it should " Properly transfer.  " in {
    val acctProbe = probeRef[Acknowledge[Account]]
    val txProbe = probeRef[Acknowledge[TransactionId]]
    val walletId = WalletId(randomId)
    val wallet = createWalletAndShootCommands(walletId, List.empty)

    val p2pId     = addAccountToWallet(wallet, randomId, AccountType.P2P)
    val spotId    = addAccountToWallet(wallet, randomId, AccountType.Spot)
    val marginId  = addAccountToWallet(wallet, randomId, AccountType.Margin)
    val poolId    = addAccountToWallet(wallet, randomId, AccountType.Pool)
    val futuresId = addAccountToWallet(wallet, randomId, AccountType.Futures)

    wallet ! Deposit(p2pId,   Money(10, ARS), acctProbe.ref)
    wallet ! Deposit(spotId,  Money(10, ARS), acctProbe.ref)
    wallet ! Deposit(marginId, Money(10, ARS), acctProbe.ref)
    wallet ! Deposit(poolId,    Money(10, ARS), acctProbe.ref)
    wallet ! Deposit(futuresId, Money(10, ARS), acctProbe.ref)


    wallet ! AttemptTransaction(p2pId, spotId, Money(1, ARS), txProbe.ref)
    txProbe.expectNoMessage()
    wallet ! AttemptTransaction(marginId, spotId, Money(1, ARS), txProbe.ref)
    wallet ! AttemptTransaction(poolId, spotId, Money(1, ARS), txProbe.ref)
    wallet ! AttemptTransaction(futuresId, spotId, Money(1, ARS), txProbe.ref)



  }
  it should "rollback a transaction" in {}
  it should "list transactions" in {}

  def createWalletAndShootCommands(walletId: WalletId, commands: List[WalletCommands.Command]) = {
    val ganbaruClientId = GanbaruClientId(randomId)
    val wallet = testKit.spawn(
      Wallet.recoverFrom(walletId, settingsProvider,
        List(
          CreateWalletWithNumber(ganbaruClientId, WalletNumber(22), WalletConfirmation(randomId), ignoredRef[Acknowledge[WalletId]])
        ) ::: commands
      )
    )
    wallet
  }

  def getWallet(wallet: ActorRef[WalletCommands.Command]): CreatedWallet = {
    val getWallet = probeRef[Acknowledge[CreatedWallet]]
    wallet ! WalletCommands.GetWallet(getWallet.ref)
    getWallet.expectMessageType[Acknowledge[CreatedWallet]](5.second).get
  }

  def addAccountToWallet(wallet: ActorRef[WalletCommands.Command], cuit: String, acctType: AccountType.AccountType): AccountId = {
    val probe = probeRef[Acknowledge[AccountId]]
    wallet ! AddAccount(cuit, acctType, ARS,  probe.ref)
    probe.expectMessageType[Acknowledge[AccountId]](5.seconds).get
  }


}