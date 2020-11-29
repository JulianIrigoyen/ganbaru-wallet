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

  val spotAccountRequest = "requests/NewSpotAccount.json"
  val marginAccountRequest = "requests/NewMarginAccount.json"
  val p2pAccountRequest = "requests/NewP2PAccount.json"

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
    createSpotAccount(walletId)
    getWalletJson(walletId).\("accounts").get.as[JsArray].value.size mustBe 1
  }

  "get an account from wallet" in {
    val walletId = createWallet
    val accountId = createSpotAccount(walletId)

    val getAccountRequest = FakeRequest(GET, s"/api/wallets/$walletId/accounts/$accountId")
    val account = route(app, getAccountRequest).get
    println(Json.prettyPrint(contentAsJson(account)))
    status(account) mustBe OK
  }

  "deposit to a wallet account" in {
    val walletId = createWallet
    val accountId = createSpotAccount(walletId)

    depositToWalletAccount(walletId, accountId, 10000)
  }

  "withdraw from a wallet account" in {
    val walletId = createWallet
    val accountId = createSpotAccount(walletId)
    depositToWalletAccount(walletId, accountId, 10000)
    withdrawFromWalletAccount(walletId, accountId, 9788)
  }

  "get the bulkiest account in a wallet" in {
    val walletId = createWallet
    val spotAccountId = createSpotAccount(walletId)
    val marginAccountId  = createMarginAccount(walletId)

    depositToWalletAccount(walletId, spotAccountId, 10000)
    depositToWalletAccount(walletId, marginAccountId, 10001)

    val request = FakeRequest(GET, s"/api/wallets/$walletId/bulkiest")
    val result = route(app, request).get
    status(result) mustBe OK
  }

  "properly transfer" in {
    val walletId = createWallet
    val spotAccountId = createSpotAccount(walletId)
    val marginAccountId = createMarginAccount(walletId)
    val p2pAccountId = createP2PAccount(walletId)

    depositToWalletAccount(walletId, spotAccountId, 10000)
    depositToWalletAccount(walletId, marginAccountId, 5000)

   /* val requestBody = Json.parse(transferRequest(spotAccountId.id, marginAccountId.id, 5000))
    val firstTransferRequest = FakeRequest(POST, s"/api/wallets/$walletId/transfer").withJsonBody(requestBody)
    val firstTransferResult = route(app, firstTransferRequest).get
    status(firstTransferResult) mustBe CREATED

    getWalletJson(walletId)*/

  }

  private def loadRequest(requestFileName: String) = {
    scala.io.Source.fromResource(requestFileName).getLines().mkString
  }

  private def createWallet: WalletId = {
    val jsonString = """{ "id": "222", "cuit": "20391718068" }"""
    val request = FakeRequest(POST, s"/api/wallets/confirm").withBody(Json.parse(jsonString))
    val result = route(app, request).get
    //println(Json.prettyPrint(contentAsJson(result)))
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

  private def createSpotAccount(walletId: WalletId) = {
    val accountRequestBody = loadRequest(spotAccountRequest)
    val postRequest = FakeRequest(POST, s"/api/wallets/$walletId/account").withJsonBody(Json.parse(accountRequestBody))
    val account = route(app, postRequest).get
    //println(Json.prettyPrint(contentAsJson(account)))
    status(account) mustBe CREATED

    AccountId((contentAsJson(account) \ "id").as[String])
  }

  private def createMarginAccount(walletId: WalletId) = {
    val accountRequestBody = loadRequest(marginAccountRequest)
    val postRequest = FakeRequest(POST, s"/api/wallets/$walletId/account").withJsonBody(Json.parse(accountRequestBody))
    val account = route(app, postRequest).get
    //println(Json.prettyPrint(contentAsJson(account)))
    status(account) mustBe CREATED

    AccountId((contentAsJson(account) \ "id").as[String])
  }

  private def createP2PAccount(walletId: WalletId) = {
    val accountRequestBody = loadRequest(p2pAccountRequest)
    val postRequest = FakeRequest(POST, s"/api/wallets/$walletId/account").withJsonBody(Json.parse(accountRequestBody))
    val account = route(app, postRequest).get
    //println(Json.prettyPrint(contentAsJson(account)))
    status(account) mustBe CREATED

    AccountId((contentAsJson(account) \ "id").as[String])
  }

  private def depositToWalletAccount(walletId: WalletId, accountId: AccountId, amount: Int) = {
    val jsonString = """{ "amount": """ + amount + """, "currency": "ARS" }"""
    val depositRequest = FakeRequest(PATCH, s"/api/wallets/$walletId/accounts/$accountId/deposit")
      .withJsonBody(Json.parse(jsonString))

    val deposit = route(app, depositRequest).get
    status(deposit) mustBe OK
  }

  def transferRequest(debitId: String, creditId: String, amount: Int) = {
    """ { 'debit': """ + debitId + """, 'credit': """ + creditId + """, 'amount': """ + amount + """, 'currency': 'ARS'  }"""
  }

  private def withdrawFromWalletAccount(walletId: WalletId, accountId: AccountId, amount: Int) = {
    val jsonString = """{ "amount": """ + amount + """, "currency": "ARS" }"""
    val withdrawRequest = FakeRequest(PATCH, s"/api/wallets/$walletId/accounts/$accountId/withdraw")
      .withJsonBody(Json.parse(jsonString))

    val withdraw = route(app, withdrawRequest).get
    status(withdraw) mustBe OK
  }
}
