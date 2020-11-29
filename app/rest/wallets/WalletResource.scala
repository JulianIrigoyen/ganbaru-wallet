package rest.wallets

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.util.Timeout
import com.google.inject.Inject
import model.AccountType.AccountType
import model.Money.Currency
import model.{Account, AccountId, Money, Transaction, TransactionId, WalletFactory}
import model.util.{Acknowledge, AcknowledgeWithFailure, AcknowledgeWithResult}
import model.wallets.Wallet.WalletConfirmation
import model.wallets.WalletCommands.{AddAccount, AttemptTransaction, Deposit, GetAccount, GetBulkiestAccount, GetWallet, ListTransactions, RollbackTransaction, Withdraw}
import model.wallets.{CreatedWallet, GandaruClientId, WalletCommands, WalletId}
import org.nullvector.api.json.JsonMapper
import play.Module.WalletsSystem
import play.api.libs.json.{util => _, _}
import play.api.mvc.{Action, AnyContent, InjectedController, Result}
import sharding.EntityProvider

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt

object WalletResource {

  /**
       Implicits used by nullvector.api.json.JsonMapper for serialization
       https://github.com/null-vector/akka-reactivemongo-plugin
                                                                          */

  implicit val configuration = rest.defaultJsonConfiguration
  implicit val cw            = JsonMapper.writesOf[CreatedWallet]
  implicit val wcpr          = JsonMapper.readsOf[WalletConfirmationPost]
  implicit val wcpw          = JsonMapper.writesOf[WalletConfirmationPost]
  implicit val tpr           = JsonMapper.readsOf[TestPost]
  implicit val tpw           = JsonMapper.writesOf[TestPost]
  implicit val acpr          = JsonMapper.readsOf[AccountPost]
  implicit val acpw          = JsonMapper.writesOf[AccountPost]
  implicit val accIdw        = JsonMapper.writesOf[AccountId]
  implicit val accw          = JsonMapper.writesOf[Account]
  implicit val txw           = JsonMapper.writesOf[Transaction]
  implicit val txr           = JsonMapper.readsOf[Transaction]
  implicit val tridw         = JsonMapper.writesOf[TransactionId]
  implicit val moneyr        = JsonMapper.readsOf[MoneyPatch]
  implicit val moneyw        = JsonMapper.writesOf[MoneyPatch]
  implicit val trpr          = JsonMapper.readsOf[TransferPost]
  implicit val trpw          = JsonMapper.writesOf[TransferPost]

  case class TestPost(id: Int, cuit: String)

  case class WalletConfirmationPost(
                                     id: String,
                                     cuit: String
                                   )

  case class AccountPost(
                        cuit: String,
                        accountType: AccountType,
                        currency: Currency
                        ) {
    def toCommand(replyTo: ActorRef[Acknowledge[AccountId]]): AddAccount =
      AddAccount(cuit, accountType, currency, replyTo)
  }

  case class MoneyPatch(
                         amount: BigDecimal,
                         currency: Currency
                       ) {

    def toDepositCommand(accountId: AccountId, replyTo: ActorRef[Acknowledge[Account]]): Deposit =
      Deposit(accountId, Money(amount, currency), replyTo)

    def toWithdrawCommand(accountId: AccountId, replyTo: ActorRef[Acknowledge[Account]]): Withdraw =
      Withdraw(accountId, Money(amount, currency), replyTo)

  }

  case class TransferPost(
                           debit: String,
                           credit: String,
                           amount: BigDecimal,
                           currency: Currency
                          ) {
    def toCommand(replyTo: ActorRef[Acknowledge[TransactionId]]): AttemptTransaction = {
      AttemptTransaction(AccountId(debit), AccountId(credit), Money(amount, currency), replyTo)
    }
  }
}

class WalletResource @Inject()(
                                walletsSystem: WalletsSystem,
                                walletProvider:  EntityProvider[WalletCommands.Command, WalletId],
                                walletFactory: EntityProvider[WalletFactory.Command, GandaruClientId]
                              )
                              (implicit ex: ExecutionContext) extends InjectedController {

  import org.nullvector.api.json.JsonMapper._
  import rest.wallets.WalletResource._

  private implicit val timeout: Timeout = Timeout(30.seconds)

  /** Implemented for TDD */
  def test(): Action[TestPost] = Action.async(parse.json[TestPost]) { request =>
    //println("RECEIVED POST")
    //println(request.body)
    Future.successful(Created)
  }

  def confirmWallet(): Action[WalletConfirmationPost] = Action.async(parse.json[WalletConfirmationPost]) { request =>
    //println(s"Received request to create wallet ${request.body}")
    val confirmation = request.body
    val clientId = GandaruClientId(confirmation.id)
    walletFactory.entityFor(clientId)
      .ask[Acknowledge[WalletId]](replyTo => WalletFactory.ConfirmWallet(WalletConfirmation(confirmation.cuit), replyTo))
      .transform(_.flatMap(_.toTry))
      .map(walletId => Created(Json.obj("wallet_id" -> walletId.id)))
  }

  def getWallet(walletId: WalletId): Action[AnyContent] = Action.async { _ =>
    //println(s"Received get request for wallet $walletId")
    walletProvider.entityFor(walletId)
      .ask[Acknowledge[CreatedWallet]](replyTo => GetWallet(replyTo))
      .map(acknowledgement => toResult[CreatedWallet](acknowledgement, Ok(_)) )
  }

  def addAccount(walletId: WalletId): Action[AccountPost] = Action.async(parse.json[AccountPost]) { request =>
    //println(s"Received POST request to add account wallet $walletId")
    val accountRequest = request.body
    walletProvider.entityFor(walletId)
      .ask[Acknowledge[AccountId]](replyTo => accountRequest.toCommand(replyTo))
      .map(acknowledgement => toResult[AccountId](acknowledgement, Created(_)))
  }

  def getAccount(walletId: WalletId, accountId: AccountId): Action[AnyContent] = Action.async { _ =>
    //println(s"Received GET request to find account $accountId for wallet $walletId")
    walletProvider.entityFor(walletId)
      .ask[Acknowledge[Account]](replyTo => GetAccount(accountId, replyTo))
      .map(acknowledgement => toResult[Account](acknowledgement, Ok(_)))
  }

  def getBulkiestAccount(walletId: WalletId): Action[AnyContent] = Action.async { _ =>
    walletProvider.entityFor(walletId)
      .ask[Acknowledge[Account]](replyTo => GetBulkiestAccount(replyTo))
      .map(acknowledgement => toResult[Account](acknowledgement, Ok(_)))
  }

  def deposit(walletId: WalletId, accountId: AccountId): Action[MoneyPatch] = Action.async(parse.json[MoneyPatch]) { request =>
    val moneyRequest = request.body
    walletProvider.entityFor(walletId)
      .ask[Acknowledge[Account]](replyTo =>  moneyRequest.toDepositCommand(accountId, replyTo))
      .map(acknowledgement => toResult[Account](acknowledgement, Ok(_)))
  }

  def withdraw(walletId: WalletId, accountId: AccountId): Action[MoneyPatch] = Action.async(parse.json[MoneyPatch]) { request =>
    val moneyRequest = request.body
    walletProvider.entityFor(walletId)
      .ask[Acknowledge[Account]](replyTo => moneyRequest.toWithdrawCommand(accountId, replyTo))
      .map(acknowledgement => toResult[Account](acknowledgement, Ok(_)))
  }

  def transfer(walletId: WalletId): Action[TransferPost] = Action.async(parse.json[TransferPost]) { request =>
    val transferRequest = request.body
    walletProvider.entityFor(walletId)
      .ask[Acknowledge[TransactionId]](replyTo => transferRequest.toCommand(replyTo))
      .map(acknowledgement => toResult[TransactionId](acknowledgement, Created(_)))
  }

  def rollbackTransaction(walletId: WalletId, transactionId: TransactionId): Action[AnyContent] = Action.async { _ =>
    walletProvider.entityFor(walletId)
      .ask[Acknowledge[TransactionId]](replyTo => RollbackTransaction(transactionId, replyTo))
      .map(acknowledgement => toResult[TransactionId](acknowledgement, Accepted(_)))
  }

  def listAccountTransactions(walletId: WalletId, accountId: AccountId): Action[AnyContent] = Action.async { _ =>
    walletProvider.entityFor(walletId)
      .ask[Acknowledge[List[Transaction]]](replyTo => ListTransactions(accountId, replyTo))
      .map(acknowledgement => toResult[List[Transaction]](acknowledgement, Ok(_)))
  }

  private def toResult[T](acknowledgement: Acknowledge[T], onSuccess: JsValue => Result )(implicit  w: Writes[T]) = {
    acknowledgement match {
      case AcknowledgeWithResult(result: T) => onSuccess(result.asJson)
      case AcknowledgeWithFailure(errorMessage) => BadRequest(errorMessage)
    }
  }
}

