package rest.wallets

import java.time.{LocalDateTime, LocalTime}

import akka.actor.ActorSystem
import akka.persistence.typed.PersistenceId
import model.AccountId
import model.wallets.{GandaruClientId, WalletId}
import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest
import play.Module.WalletsSystem
import play.api.libs.json.JsValue._
import play.api.libs.json.{JsArray, JsValue, Json}
import play.api.test.Helpers._
import play.api.test._

class WalletResourceSpec extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll {

  val newAccountRequest = "requests/NewAccount.json"

  "properly post" in {
    val jsonString = """{ "id": 222, "cuit": "20391718068" }"""
    val request = FakeRequest(POST, s"/api/wallets/test").withBody(Json.parse(jsonString))
    val result = route(app, request).get
    status(result) mustBe CREATED
  }

  "confirm a wallet" in {
    createWallet mustBe a[WalletId]
  }

  "get a wallet" in {
    //println(getWalletJson(createWallet))
    getWalletJson(createWallet) mustBe a[JsValue]
  }

  "add an account to a wallet " in {
    val walletId = createWallet
    createAccount(walletId)
    getWalletJson(walletId).\("accounts").get.as[JsArray].value.size mustBe 1
  }

  "get an account from wallet" in {
    val walletId = createWallet
    val accountId = createAccount(walletId)

    val getAccountRequest = FakeRequest(GET, s"/api/wallets/$walletId/accounts/$accountId")
    val account = route(app, getAccountRequest).get
    println(Json.prettyPrint(contentAsJson(account)))
    status(account) mustBe OK
  }

  "get the bulkiest account in a wallet" in {
    //TODO after implementing transactions

  }

  private def loadRequest(requestFileName: String) = {
    scala.io.Source.fromResource(requestFileName).getLines().mkString
  }

  private def createWallet: WalletId = {
    val jsonString = """{ "id": "222", "cuit": "20391718068" }"""
    val request = FakeRequest(POST, s"/api/wallets/confirm").withBody(Json.parse(jsonString))
    val result = route(app, request).get
    println(Json.prettyPrint(contentAsJson(result)))
    status(result) mustBe CREATED
    WalletId((contentAsJson(result) \ "wallet_id").as[String])
  }

  private def getWalletJson(walletId: WalletId) = {
    val getRequest = FakeRequest(GET, s"/api/wallets/$walletId")
    val wallet = route(app, getRequest).get
    println(Json.prettyPrint(contentAsJson(wallet)))
    status(wallet) mustBe OK
    contentAsJson(wallet)
  }

  private def createAccount(walletId: WalletId) = {
    val accountRequestBody = loadRequest(newAccountRequest)
    val postRequest = FakeRequest(POST, s"/api/wallets/$walletId/account").withJsonBody(Json.parse(accountRequestBody))
    val account = route(app, postRequest).get
    println(Json.prettyPrint(contentAsJson(account)))
    status(account) mustBe CREATED

    AccountId((contentAsJson(account) \ "id").as[String])
  }
}
