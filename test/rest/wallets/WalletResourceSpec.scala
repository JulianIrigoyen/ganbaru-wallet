package rest.wallets

import java.time.{LocalDateTime, LocalTime}

import akka.actor.ActorSystem
import akka.persistence.typed.PersistenceId
import model.wallets.GandaruClientId
import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest
import play.Module.WalletsSystem
import play.api.libs.json.{JsArray, JsValue, Json}
import play.api.test.Helpers._
import play.api.test._

class WalletResourceSpec extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll {


  "properly post" in {
    val jsonString = """{ "id": 222, "cuit": "20391718068" }"""
    val request = FakeRequest(POST, s"/api/test").withBody(Json.parse(jsonString))
    val result = route(app, request).get
    status(result) mustBe OK
  }

  "confirm a wallet" in {
    val id = GandaruClientId("222")
    val jsonString = """{ "id": "222", "cuit": "20391718068" }"""

    val request = FakeRequest(POST, s"/api/confirm").withBody(Json.parse(jsonString))
    val result = route(app, request).get
    println(Json.prettyPrint(contentAsJson(result)))
    status(result) mustBe CREATED

  }


}
